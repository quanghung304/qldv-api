package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.gateway.*;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.service.import_file.ImportFileConfigDetailService;
import com.agribank.qldv_api.service.import_file.ImportFileConfigService;
import com.agribank.qldv_api.service.import_file.ImportFileService;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.response.BaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service("ImportDVService")
public class ImportDVService extends ImportFileService {
    @Autowired
    private DVClient dVClient;

    @Autowired
    private DVDraftClient dvDraftClient;

    @Autowired
    private OrganizationClient organizationClient;

    @Autowired
    private DVService dvService;
    @Autowired
    private ReligionClient religionClient;
    @Autowired
    private EthnicClient ethnicClient;

    public ImportDVService(ImportFileConfigService importFileConfigService, ImportFileConfigDetailService importFileConfigDetailService) {
        super(importFileConfigService, importFileConfigDetailService);
    }

    protected BaseResponse<T> handleSaveDataUpload(BaseResponse response) {
        Integer maxRow = 50;
        List<Map<String, Object>> dataImport = (List<Map<String, Object>>) response.getData();

        List<List<Map<String, Object>>> batchs = new ArrayList<>();
        List<Map<String, Object>> currentBatch = new ArrayList<>();

        for (int i = 0; i < dataImport.size(); i++) {
            if (Objects.nonNull(dataImport.get(i).get("staffCode")) && !dataImport.get(i).get("staffCode").equals("")) {
                currentBatch.add(dataImport.get(i));
            }
            if ((i > 0 && i % maxRow == 0) || i == dataImport.size() - 1) {
                batchs.add(currentBatch);
                currentBatch = new ArrayList<>();
            }
        }

        UserDetailsImpl userDetails = getUserRequested();
        ObjectMapper objectMapper = new ObjectMapper();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (List<Map<String, Object>> oneBatch : batchs) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    List<DvDraft> createDvDrafts = new ArrayList<>();
                    List<DvDraft> updateDvDrafts = new ArrayList<>();
                    List<DV> oldDVs = new ArrayList<>();

                    List<String> staffCodes = new ArrayList<>();
                    for(Map<String, Object> dataItem : oneBatch){
                        if (Objects.nonNull(dataItem.get("staffCode")) && dataItem.get("staffCode") != ""){
                            staffCodes.add((String) dataItem.get("staffCode"));
                        }
                    }

                    List<DV> preDvs = dVClient.findByStaffCodes(staffCodes).getData();
                    List<DvDraft> preDVDrafts = dvDraftClient.findByStaffCodes(staffCodes).getData();

                    Map<String, DV> mapStaffCodeToDV = new HashMap<>();
                    Map<String, DvDraft> mapStaffCodeToDvDraft = new HashMap<>();

                    for (DV preDV: preDvs){
                        mapStaffCodeToDV.put(preDV.getStaffCode(), preDV);
                    }

                    for (DvDraft preDVDraft: preDVDrafts){
                        mapStaffCodeToDvDraft.put(preDVDraft.getStaffCode(), preDVDraft);
                    }

                    List<Organization> organizations = organizationClient.findAll().getData();
                    Set<String> setOrganizationCodes = organizations.stream().map(x-> x.getCode()).collect(Collectors.toSet());

                    List<Religion> religions = religionClient.findAll().getData();
                    Set<String> setReligion = religions.stream().map(x->x.getName()).collect(Collectors.toSet());

                    List<Ethnic> ethnics = ethnicClient.findAll().getData();
                    Set<String> setEthnic = ethnics.stream().map(x->x.getName()).collect(Collectors.toSet());

                    for(Map<String, Object> dataItem : oneBatch){
                        DvDraft dvDraft = objectMapper.convertValue(dataItem, DvDraft.class);
                        if (dvDraft.getOrganizationCode()!= null && (dvDraft.getOrganizationCode() == "" && !setOrganizationCodes.contains(dvDraft.getOrganizationCode()))){
                            continue;
                        }

                        if (dvDraft.getEthnic()!= null && (dvDraft.getEthnic() == "" && !setEthnic.contains(dvDraft.getEthnic()))){
                            continue;
                        }

                        if (dvDraft.getReligion()!= null && (dvDraft.getReligion() == "" && !setReligion.contains(dvDraft.getReligion()))){
                            continue;
                        }

                        if (dvDraft.getStaffCode() == "" || mapStaffCodeToDvDraft.containsKey(dvDraft.getStaffCode())){
                            continue;
                        }

                        dvDraft.setStatus(EApprovalStatus.PENDING.getId());
                        dvDraft.setCreatedBy(userDetails.getId());

                        if (mapStaffCodeToDV.containsKey(dvDraft.getStaffCode())){
                            oldDVs.add(mapStaffCodeToDV.get(dvDraft.getStaffCode()));
                            updateDvDrafts.add(dvDraft);
                        } else {
                            createDvDrafts.add(dvDraft);
                        }

                    }

                    dvService.createManyDV(createDvDrafts);
                    dvService.updateManyDV(oldDVs, updateDvDrafts);

                } catch (Exception e) {
                    System.out.println("EEEEE");
                    System.out.println(e);
                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        response.setData(null);
        response.setSuccess(Boolean.TRUE);
        response.setMessage("Import đảng viên thành công");

        return response;
    }


    protected BaseResponse<T> handleSaveDataUploadWithoutThread(BaseResponse response) {
        UserDetailsImpl userDetails = getUserRequested();
        ObjectMapper objectMapper = new ObjectMapper();

        List<Map<String, Object>> dataImport = (List<Map<String, Object>>) response.getData();
        List<DvDraft> createDvDrafts = new ArrayList<>();
        List<DvDraft> updateDvDrafts = new ArrayList<>();
        List<DV> oldDVs = new ArrayList<>();

        List<String> staffCodes = new ArrayList<>();
        for(Map<String, Object> dataItem : dataImport){
            if (Objects.nonNull(dataItem.get("staffCode")) && dataItem.get("staffCode") != ""){
                staffCodes.add((String) dataItem.get("staffCode"));
            }
        }

        List<DV> preDvs = dVClient.findByStaffCodes(staffCodes).getData();
        List<DvDraft> preDVDrafts = dvDraftClient.findByStaffCodes(staffCodes).getData();

        Map<String, DV> mapStaffCodeToDV = new HashMap<>();
        Map<String, DvDraft> mapStaffCodeToDvDraft = new HashMap<>();

        for (DV preDV: preDvs){
            mapStaffCodeToDV.put(preDV.getStaffCode(), preDV);
        }

        for (DvDraft preDVDraft: preDVDrafts){
            mapStaffCodeToDvDraft.put(preDVDraft.getStaffCode(), preDVDraft);
        }

        List<Organization> organizations = organizationClient.findAll().getData();
        Set<String> setOrganizationCodes = organizations.stream().map(x-> x.getCode()).collect(Collectors.toSet());

        List<Religion> religions = religionClient.findAll().getData();
        Set<String> setReligion = religions.stream().map(x->x.getName()).collect(Collectors.toSet());

        List<Ethnic> ethnics = ethnicClient.findAll().getData();
        Set<String> setEthnic = ethnics.stream().map(x->x.getName()).collect(Collectors.toSet());

        for(Map<String, Object> dataItem : dataImport){
            DvDraft dvDraft = objectMapper.convertValue(dataItem, DvDraft.class);
            if (dvDraft.getOrganizationCode()!= null && (dvDraft.getOrganizationCode() == "" && !setOrganizationCodes.contains(dvDraft.getOrganizationCode()))){
                continue;
            }

            if (dvDraft.getEthnic()!= null && (dvDraft.getEthnic() == "" && !setEthnic.contains(dvDraft.getEthnic()))){
                continue;
            }

            if (dvDraft.getReligion()!= null && (dvDraft.getReligion() == "" && !setReligion.contains(dvDraft.getReligion()))){
                continue;
            }

            if (dvDraft.getStaffCode() == "" || mapStaffCodeToDvDraft.containsKey(dvDraft.getStaffCode())){
                continue;
            }

            dvDraft.setStatus(EApprovalStatus.PENDING.getId());
            dvDraft.setCreatedBy(userDetails.getId());

            if (mapStaffCodeToDV.containsKey(dvDraft.getStaffCode())){
                oldDVs.add(mapStaffCodeToDV.get(dvDraft.getStaffCode()));
                updateDvDrafts.add(dvDraft);
            } else {
                createDvDrafts.add(dvDraft);
            }

        }

        dvService.createManyDV(createDvDrafts);
        dvService.updateManyDV(oldDVs, updateDvDrafts);

        response.setData(null);
        response.setSuccess(Boolean.TRUE);
        response.setMessage("Import đảng viên thành công");

        return response;
    }

    private UserDetailsImpl getUserRequested(){
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
