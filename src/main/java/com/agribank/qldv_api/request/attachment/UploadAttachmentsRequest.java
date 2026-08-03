package com.agribank.qldv_api.request.attachment;

import com.agribank.qldv_api.exception.FieldValidationException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * {@link #validate(long)} CHỈ gồm phần KHÔNG cần truy vấn DB (đúng đuôi + đúng magic bytes, kích
 * thước từng file, kích thước tổng, trùng tên trong 1 lượt upload) — gọi ở controller TRƯỚC khi
 * vào service, cùng convention với {@code UploadDocumentTemplateRequest}. Phần validate CẦN DB
 * (case tồn tại, scope, case chưa hoàn thành) nằm ở {@code AttachmentService}.
 *
 * Kiểm tra magic bytes (không chỉ tin đuôi file người dùng đặt) — chặn trường hợp đổi đuôi file để
 * qua mặt validate định dạng.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadAttachmentsRequest {
    private static final long MAX_FILE_SIZE_KB = 20480; // 20MB — giới hạn cứng theo đúng yêu cầu prompt
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx", "jpg", "jpeg", "png");

    List<MultipartFile> files;

    public void validate(long maxTotalSizeKb) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (files == null || files.isEmpty()) {
            errors.put("files", "Phải chọn ít nhất 1 file để đính kèm");
            throwIfInvalid(errors);
            return;
        }

        Set<String> seenNames = new HashSet<>();
        long totalSizeKb = 0;
        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                errors.put("files", "Tên file không được để trống");
                continue;
            }
            if (!seenNames.add(originalName.toLowerCase(Locale.ROOT))) {
                errors.put(originalName, "Trùng tên file trong cùng 1 lượt upload — vui lòng đổi tên trước khi tải lên");
                continue;
            }

            long sizeKb = file.getSize() / 1024;
            totalSizeKb += sizeKb;
            if (sizeKb > MAX_FILE_SIZE_KB) {
                errors.put(originalName, "Dung lượng file vượt quá " + MAX_FILE_SIZE_KB + " KB (20MB)");
                continue;
            }

            String extension = extractExtension(originalName);
            if (extension == null || !ALLOWED_EXTENSIONS.contains(extension)) {
                errors.put(originalName, "Định dạng file không hợp lệ — chỉ chấp nhận pdf/doc/docx/jpg/jpeg/png");
                continue;
            }
            if (!matchesMagicBytes(file, extension)) {
                errors.put(originalName, "Nội dung file không khớp định dạng ." + extension
                        + " (đuôi file có thể đã bị đổi tên)");
            }
        }

        if (totalSizeKb > maxTotalSizeKb) {
            errors.put("files", "Tổng dung lượng tất cả file vượt quá " + maxTotalSizeKb + " KB");
        }

        throwIfInvalid(errors);
    }

    private static String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return null;
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean matchesMagicBytes(MultipartFile file, String extension) {
        byte[] header = readHeader(file, 8);
        if (header == null) {
            return false;
        }
        return switch (extension) {
            case "pdf" -> matches(header, 0x25, 0x50, 0x44, 0x46);
            case "doc" -> matches(header, 0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1);
            case "docx" -> matches(header, 0x50, 0x4B, 0x03, 0x04);
            case "jpg", "jpeg" -> matches(header, 0xFF, 0xD8, 0xFF);
            case "png" -> matches(header, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            default -> false;
        };
    }

    private static byte[] readHeader(MultipartFile file, int length) {
        try (InputStream is = file.getInputStream()) {
            byte[] buffer = new byte[length];
            int read = is.read(buffer);
            if (read <= 0) {
                return null;
            }
            return read == length ? buffer : Arrays.copyOf(buffer, read);
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean matches(byte[] header, int... signature) {
        if (header.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((header[i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private void throwIfInvalid(Map<String, String> errors) {
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }
}
