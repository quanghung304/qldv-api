<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" extension-element-prefixes="ex">
    <xsl:decimal-format name="decimalFormat" decimal-separator="," grouping-separator="." zero-digit="0" digit="#" pattern-separator=";"></xsl:decimal-format>
    <xsl:decimal-format name="number" decimal-separator="," grouping-separator="." />
    <xsl:output method="html" />
    <xsl:template match="DuLieu">
        <xsl:text disable-output-escaping="yes">&lt;</xsl:text>!DOCTYPE html<xsl:text disable-output-escaping="yes">&gt;</xsl:text>
        <html>
            <head>
                <title><xsl:value-of disable-output-escaping="yes" select="TTinChung/BaoCao" /></title>
                <style>
                    body {
                    padding: 0;
                    margin: 0;
                    }

                    body, table {
                    font-size: 14px;
                    line-height: 25px;
                    text-align: center;
                    font-family: "Times New Roman";
                    color: rgb(0, 0, 0);
                    }

                    .container {
                    width: 965px;
                    overflow: visible;
                    position: relative;
                    }

                    .ruler-template{
                    padding-top: 40px;
                    margin-left: 45px;
                    margin-right: 20px;
                    box-sizing: border-box;
                    }

                    .content-detail{
                    min-height: 1068px;
                    max-width: 775px;
                    }

                    .flex {
                    display: flex;
                    }

                    .justify-center {
                    justify-content: center
                    }

                    .p-relative {
                    position: relative;
                    }

                    .font-bold {
                    font-weight: bold;
                    }

                    .font-italic {
                    font-style: italic;
                    }

                    .text-left {
                    text-align: left;
                    }

                    .text-center {
                    text-align: center;
                    }

                    .text-right {
                    text-align: right;
                    }

                    #tbDetail .tr-header td {
                    padding: 2px 0;
                    }

                    .width-full{
                    width:100%;
                    }

                    .width-half{
                    width:50%;
                    }

                    .width-triple{
                    width:33.33%;
                    }

                    .two-dot{
                    padding-right: 4px;
                    }

                    .flex-1{
                    flex: 1
                    }

                    .header-title{
                    padding: 10px 0px;
                    }

                    .condition-filter{
                    border-bottom: 1px solid #ccc;
                    padding: 5px 0px;
                    }

                    .table-detail{
                    padding: 5px 0px;
                    }

                    #tbDetail, #tbFooter {
                    border-style: solid;
                    border-color: #776b6b;
                    border-width: 1px;
                    }

                    #tbDetail{
                    border: none;
                    }
                    #tbDetail th:first-child, #tbFooter td:first-child, #tbDetail td:first-child{
                    border-left: 1px solid #776b6b;
                    }

                    #tbDetail tr:last-child th{
                    border-bottom: 1px solid #776b6b;
                    }

                    #tbDetail tr:first-child th, #tbDetail tr:first-child td{
                    border-top: 1px solid #776b6b;
                    border-bottom: 1px solid #776b6b;
                    }

                    #tbDetail .tr-header th{
                    padding: 2px 0;
                    }

                    #tbDetail th, #tbDetail td{
                    border-style: none solid dotted none;
                    border-color: #776b6b;
                    border-width: 1px;
                    padding: 2px;
                    border-bottom-style: solid;
                    }

                    #tbDetail{
                    border-collapse: collapse;
                    }

                    #tbDetail td .edit-value, #tbDetail td .edit-label, #tbDetail td .edit-label-en {
                    padding: 0 2px;
                    }

                    #tbDetail tr td span, #tbFooter tr td span  {
                    display: inline-block;
                    }

                    .number {
                    text-align: right;
                    }

                    .float-left{
                    float: left;
                    }

                    .float-right{
                    float: right;
                    }

                    .clear{
                    clear:both;
                    }

                    .display-table-cell{
                    display: table-cell;
                    }

                    .word-break{
                    white-space: break-spaces;
                    }

                    .break-word{
                    word-break: break-all;
                    }

                    .vertical-top{
                    vertical-align: top;
                    }

                    .title-color{
                    background-color: rgb(153 0 0);
                    color: #ffffff;
                    padding: 5px;
                    font-weight: 700
                    }

                    .statistic-result-two{
                    display: flex;
                    margin-top: 15px;
                    }

                    .statistic-result-two .result-value{
                    border: 1px solid;
                    margin-left: 259px;
                    padding: 10px 5px;
                    }
                </style>
            </head>
            <body onload="customData()">
                <div class="container">
                    <div class="page-info" style="display: none">
                        <div class="page-size">A4</div>
                    </div>
                    <div class="auContent ruler-template">
                        <div class="content-detail" style="">
                            <div class="header-block">
                                <div class="header-first" style="display: flex;">
                                    <table>
                                        <tr>
                                            <td style="vertical-align: top">
                                                <div style="display: flex; flex-direction: column; align-items: center">
                                                    <div class="text-center">ĐẢNG BỘ NHNo&amp;PTNT VIỆT NAM</div>
                                                    <div class="text-center">ĐẢNG ỦY/CHI BỘ: <xsl:value-of disable-output-escaping="yes" select="TTinChung/ToChucDang" />.</div>
                                                </div>
                                            </td>
                                            <td style="width : 250px">
                                            </td>
                                            <td style="vertical-align: top">
                                                <div style="display: flex; flex-direction: column; align-items: center">
                                                    <div class="text-center font-bold">ĐẢNG CỘNG SẢN VIỆT NAM</div>
                                                </div>
                                            </td>
                                        </tr>
                                    </table>
                                </div>
                                <div class="clear"></div>
                                <div class="header-title">
                                    <table class="width-full">
                                        <tr>
                                            <td class="text-center">
                                                <div class="font-bold" style="font-size: 20px"><xsl:value-of disable-output-escaping="yes" select="TTinChung/BaoCao" /></div>
                                            </td>
                                        </tr>
                                    </table>
                                </div>
                            </div>
                            <div class="clear"></div>
                            <div class="table-detail p-relative">
                                <table id="tbDetail" cellpadding="0" cellspacing="0" width="100%">
                                    <thead>
                                        ##TableHeader##
                                    </thead>
                                    <tbody>
                                        <xsl:choose>
                                            <xsl:when test="normalize-space(KetQua/DoiTuong) = ''">
                                                <tr class="tr-data-detail text-left">
                                                    <td data-field="LineNumber" style="line-height: 20px" colspan="10">
                                                        Không tìm thấy dữ liệu
                                                    </td>
                                                </tr>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:for-each select="KetQua/DoiTuong">
                                                    <tr class="tr-data-detail text-left vertical-top">
                                                        <xsl:for-each select="Item">
                                                            <xsl:variable name="rowStyle">
                                                                <xsl:choose>
                                                                    <xsl:when test="normalize-space(@style) != ''">
                                                                        <xsl:value-of select="@style"/>
                                                                    </xsl:when>
                                                                    <xsl:otherwise>
                                                                        white-space: break-spaces
                                                                    </xsl:otherwise>
                                                                </xsl:choose>
                                                            </xsl:variable>
                                                            <xsl:variable name="colSpan">
                                                                <xsl:choose>
                                                                    <xsl:when test="normalize-space(@colSpan) != ''">
                                                                        <xsl:value-of select="@colSpan"/>
                                                                    </xsl:when>
                                                                    <xsl:otherwise>
                                                                        1
                                                                    </xsl:otherwise>
                                                                </xsl:choose>
                                                            </xsl:variable>
                                                            <td colspan="{$colSpan}">
                                                                <div class="edit-value text-center" style="{$rowStyle}">
                                                                    <xsl:value-of disable-output-escaping="yes" select="GiaTri" />
                                                                </div>
                                                            </td>
                                                        </xsl:for-each>
                                                    </tr>
                                                </xsl:for-each>
                                                ##TableSummary##
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </tbody>
                                </table>
                            </div>
                            <div class="clear"></div>
                            <div class="sign-xml-block" style="margin-top: 10px">
                                <table class="width-full">
                                    <tr>
                                        <td class="width-triple">
                                            <div>
                                                <div class="edit-label font-bold">LẬP BIỂU</div>
                                            </div>
                                        </td>
                                        <td class="width-triple">
                                            <div>
                                                <div class="edit-label font-bold">KIỂM SOÁT</div>
                                            </div>
                                        </td>
                                        <td class="width-triple">
                                            <div>
                                                <div class="edit-label font-bold">T/M CẤP ỦY/CHI BỘ</div>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </div>
                    <div class="fakeContent"></div>
                </div>
                <script>
                    <xsl:text disable-output-escaping="yes">
                        <![CDATA[
        var curHeight = 0,
        index = 1,
        stopTable = false,
        heightTable = 0,
        maxHeight = 1280,
        rowDetail = 0,
        pageNumber = 0,
        numRowBreak = 0,
        hasSplitTr = false;

        String.prototype.replaceAll = function(search, replacement) {
            var target = this;
            return target.split(search).join(replacement);
        };

        function customData() {
            var auContent = document.getElementsByClassName("auContent")[0],
                fakeContent = document.getElementsByClassName("fakeContent")[0];
            auContent.style.display = "none";
            fakeContent.classList.add("ruler-template");
            var count = 0;
            do {
                addNewPage();
                addTableHeader();
                curHeight = 0;
                var page = fakeContent.getElementsByClassName("page-" + pageNumber)[0];
                var heightHeaderBlock = page.getElementsByClassName("header-block");
                if(heightHeaderBlock && heightHeaderBlock.length > 0) {
                    curHeight = heightHeaderBlock[0].offsetHeight
                }

                if (count == 0 && fakeContent.getElementsByClassName("table-detail")[0]) {
                    heightTable = fakeContent.getElementsByClassName("table-detail")[0].offsetHeight;
                }
                curHeight += heightTable;
                addRowDetail(count);
                if(numRowBreak > 1){
                    if(!hasSplitTr){
                        hasSplitTr = true;
                    }
                    count = splitRowDetail(count);
                }
                count += 1;
            }
            while (!stopTable);
            addFooterPage();
            auContent.remove();
        }

        function splitRowDetail(count){
            var table = document.getElementById('tbDetail');
            var trs = table.getElementsByTagName('tr');
            var curTr = trs[index + 1];
            var tds = curTr.getElementsByTagName('td');
            var maxSizeIndex = 0;
            var maxSize = 0;
            for(var i=0; i<tds.length; i++){
                var tdSize = tds[i].innerText;
                if(tdSize.length > maxSize){
                    maxSize = tdSize.length;
                    maxSizeIndex = i;
                }
            }
            var maxContent = tds[maxSizeIndex].innerText;
            var auContent = document.getElementsByClassName("auContent")[0];
            auContent.style.display = "block";
            var tdHeight = tds[maxSizeIndex].offsetHeight;
            auContent.style.display = "none";
            var prevPage = document.getElementsByClassName("page-" + (pageNumber - 1))[0];
            var currentPage = document.getElementsByClassName("page-" + pageNumber)[0];

            var pageProcess = 0;
            var curTrPage = currentPage.querySelectorAll(".tr-data-detail");
            if(curTrPage.length == 0){
                pageProcess = 1;
            }
            if(!prevPage){
                pageProcess = 0;
            }
            var curPageHeight = currentPage.offsetHeight;
            if(pageProcess == 1){
                curPageHeight = prevPage.offsetHeight;
            }
            var containHeight = maxHeight - curPageHeight;
            var indexBreak = Number.parseInt(maxSize * (containHeight / tdHeight)) - 50;
            if(maxContent[indexBreak] != ' '){
                for(let i=indexBreak; i>=0; i--){
                    if(maxContent[i] == ' '){
                        indexBreak = i;
                        break;
                    }
                }
            }

            tds[maxSizeIndex].innerText = maxContent.substring(0, indexBreak);
            var tempTr = document.createElement("tr");
            tempTr.setAttribute("class", curTr.getAttribute("class"));
            tempTr.setAttribute("style", curTr.getAttribute("style"));
            tempTr.innerHTML = curTr.innerHTML;
            var tempTds = tempTr.querySelectorAll('td');
            for(var i=0; i<tempTds.length; i++){
                if(i == maxSizeIndex){
                    tempTds[i].innerText = maxContent.substring(indexBreak);
                }
                else{
                    tempTds[i].innerHTML = '';
                }
            }
            if (curTr.nextSibling) {
                curTr.parentNode.insertBefore(tempTr, curTr.nextSibling);
            } else {
                curTr.parentNode.appendChild(tempTr);
            }
            tds[maxSizeIndex].innerHTML = tds[maxSizeIndex].innerHTML.replaceAll("<br>", "");
            var nextTd = curTr.nextSibling.querySelectorAll("td")[maxSizeIndex];
            nextTd.innerHTML = nextTd.innerHTML.replaceAll("<br>", "");
            if(pageProcess == 1){
                var numberRowRemove = prevPage.querySelectorAll(".tr-data-detail");
                index = index - numberRowRemove.length;
                currentPage.remove();
                prevPage.remove();
                pageNumber = pageNumber - 2;
                if(index < 1){
                    index = 1;
                }
                return count - 2;
            }
            else{
                index = index - curTrPage.length;
                currentPage.remove();
                pageNumber = pageNumber - 1;
                if(index < 1){
                    index = 1;
                }
                return count - 1;
            }
        }

        function addNewPage() {
            pageNumber += 1
            var fakeContent = document.getElementsByClassName("fakeContent")[0];
            var newPage = document.createElement("div");
            // add lại header
            newPage.setAttribute("class", "page-" + pageNumber);
            newPage.setAttribute("page", pageNumber);
            if (pageNumber > 1) {
                newPage.style.pageBreakBefore = "always";
                newPage.style.paddingTop = "40px";
            }
            fakeContent.appendChild(newPage);
            if (pageNumber == 1) {
                addTempHeader(pageNumber);
            }
        }

        function addTempHeader(numberPage) {
            var headerBlock = document.getElementsByClassName("header-block")[0],
                page = document.getElementsByClassName("page-" + numberPage)[0];
            //loại bỏ phần header thằng thật bổ sung thằng fake
            var tempHeader = document.createElement("div");
            tempHeader.className = "header-block";
            tempHeader.innerHTML = headerBlock.innerHTML;
            page.appendChild(tempHeader);

            if (numberPage <= 1) {
                var auContent = document.getElementsByClassName("auContent")[0];
                var fakeContent = document.getElementsByClassName("fakeContent")[0];
            }
        }

        function addTableHeader() {
            var page = document.getElementsByClassName("page-" + pageNumber)[0],
                tempTable = document.createElement("table"),
                tempTbody = document.createElement("tbody"),
                table = document.getElementById('tbDetail');
            if (table) {
                var trs = table.querySelectorAll(".tr-header");
                tempTable.setAttribute("id", "tbDetail");
                tempTable.setAttribute("cellpadding", "0");
                tempTable.setAttribute("cellspacing", "0");
                tempTable.setAttribute("width", "100%");
                tempTable.setAttribute("class", "table-detail");
                for(let i=0; i<trs.length; i++){
                    var tempTr = document.createElement("tr");
                    tempTr.setAttribute("class", trs[i].getAttribute("class"));
                    tempTr.setAttribute("style", trs[i].getAttribute("style"));
                    tempTr.innerHTML = trs[i].innerHTML;
                    tempTbody.appendChild(tempTr);
                }
                tempTable.appendChild(tempTbody);
                page.appendChild(tempTable);
            }
        }

        function addRowDetail(curTable) {
            var page = document.getElementsByClassName("page-" + pageNumber)[0],
                tempTableDetail = page.getElementsByClassName("table-detail")[0],
                table = document.getElementById('tbDetail');
            if (table) {
                var trs = table.getElementsByTagName('tr'),
                    step = curTable == 0 ? 0 : 1;
                if (index + step >= trs.length) {
                    stopTable = true;
                }
                for (i = (index + step); i < trs.length; i++) {
                    if (trs[i].classList.contains("tr-data-detail")) {
                        var prevHeight = page.offsetHeight;
                        var tempTbody = tempTableDetail.getElementsByTagName("tbody");
                        var tempTr = document.createElement("tr");
                        tempTr.setAttribute("class",  trs[i].getAttribute("class"));
                        tempTr.setAttribute("style",  trs[i].getAttribute("style"));
                        tempTr.innerHTML = trs[i].innerHTML;
                        tempTbody[0].appendChild(tempTr);
                        curHeight = page.offsetHeight;
                        rowDetail += 1;
                        if (curHeight > maxHeight) {
                            tempTbody[0].removeChild(tempTr);
                            i--;
                            index = i;
                            numRowBreak++;
                            if(prevHeight < (maxHeight * 2 / 3)){
                                numRowBreak++;
                            }
                            else{
                                var numberRowTr = tempTbody[0].querySelectorAll(".tr-data-detail");
                                if(numberRowTr.length == 0){
                                    numRowBreak++;
                                }
                            }
                            break;
                        }
                        else{
                            numRowBreak = 0;
                        }
                    }

                    if (i == (trs.length - 1)) {
                        stopTable = true;
                    }

                }
            } else {
                stopTable = true;
            }
        }

        function addFooterPage() {
            var page = document.getElementsByClassName("page-" + pageNumber)[0],
                signBlock = document.getElementsByClassName("sign-xml-block")[0],
                tempDivSignBlock = document.createElement("div");

            tempDivSignBlock.className = "sign-xml-block";
            tempDivSignBlock.setAttribute("style", signBlock.getAttribute("style"));
            tempDivSignBlock.innerHTML = signBlock.innerHTML;

            page.appendChild(tempDivSignBlock);
            if (page.offsetHeight > maxHeight) {
                page.removeChild(tempDivSignBlock);
                addNewPage();
                page = document.getElementsByClassName("page-" + pageNumber)[0];
                page.appendChild(tempDivSignBlock);
            }
        }
]]>
                    </xsl:text>
                </script>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>