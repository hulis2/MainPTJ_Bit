<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- 캠핑장 목록 페이지
캠핑장 검색 결과에 맞는 캠핑장 목록 출력
캠핑장 이미지 클릭 시 상세정보 페이지 전환
한 페이지에 5개의 캠핑장 출력, 페이지 네이게이션 및 조회수, 평점, 등록일 별 소팅 기능
작성자 : 박철홍 -->
<!DOCTYPE html>
<html lang="en-US" dir="ltr">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- Document Title -->
    <title>ListCamp</title>
    
    <jsp:include page="../../resources/commonLib.jsp"/>

    <style type="text/css">
      .camp_name_sub {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    </style>
        
  </head>
  <body data-spy="scroll" data-target=".onpage-navigation" data-offset="60">
    <main>
          
      <!-- header -->      
      <jsp:include page="/view/common/header.jsp"/>
      <!-- header End -->

      <!-- Search -->
      <jsp:include page="/view/camp/campSearch.jsp"/>
      <!-- Search End -->

      <div class="container"  style="padding-top: 30px; padding-right: 15px; padding-left: 15px; margin-right: auto; margin-left: auto" >   
          
        <div>
        <!-- 상단 -->
        <div class="row">
        
           <div class="col-xs-9">전체  
             <span style="color: rgb(230, 173, 17);">${resultPage.totalCount}개</span> 캠핑장, 현재 ${resultPage.currentPage}  페이지
           </div>
                
                <div class="fa-hover col-xs-3 ">  
                  조회수
                  <i class="fa fa-arrow-down order" name="sortCondition" style="cursor: pointer;" data="조회수 높은순"></i>
                                   
                  평점
                  <i class="fa fa-arrow-down order" name="sortCondition" style="cursor: pointer;" data="평점 높은순"></i>
                  
                  등록일
                  <i class="fa fa-arrow-down order" name="sortCondition" style="cursor: pointer;" data="최근 등록일순"></i>
                </div>
          
        </div>

        <form id="order">
          <input type="hidden" id="sortCondition" name="sortCondition" value="">
          <input type="hidden" name="searchKeyword" value="${search.searchKeyword}">
          <c:set var="i" value="0" />
            <c:forEach var="campAddr" items="${search.campAddr}">
              <c:set var="i" value="${ i+1 }" />
              <input type="hidden" name="campAddr" value="${campAddr}">
            </c:forEach>
            <c:forEach var="detailCampAddr" items="${search.detailCampAddr}">
              <c:set var="i" value="${ i+1 }" />
              <input type="hidden" name="detailCampAddr" value="${detailCampAddr}">
            </c:forEach>
            <c:forEach var="circumstance" items="${search.circumstance}">
                <c:set var="i" value="${ i+1 }" />
                <input type="hidden" name="circumstance" value="${circumstance}">
            </c:forEach>
            <c:forEach var="mainSite" items="${search.mainSite}">
                <c:set var="i" value="${ i+1 }" />
                <input type="hidden" name="mainSite" value="${mainSite}">
            </c:forEach>
            <c:forEach var="subSite" items="${search.subSite}">
                <c:set var="i" value="${ i+1 }" />
                <input type="hidden" name="subSite" value="${subSite}">
            </c:forEach>
            <c:forEach var="theme" items="${search.theme}">
                <c:set var="i" value="${ i+1 }" />
                <input type="hidden" name="theme" value="${theme}">
            </c:forEach>
            <c:forEach var="price" items="${search.price}">
                <c:set var="i" value="${ i+1 }" />
                <input type="hidden" name="price" value="${price}">
            </c:forEach>
        </form>

        <form id="pagenavi">
          <input type="hidden" name="sortCondition" value="${search.sortCondition}">
          <input type="hidden" name="searchKeyword" value="${search.searchKeyword}">
          <c:set var="i" value="0" />
           <c:forEach var="campAddr" items="${search.campAddr}">
              <c:set var="i" value="${ i+1 }" />
              <input type="hidden" name="campAddr" value="${campAddr}">
          </c:forEach>
          <c:forEach var="detailCampAddr" items="${search.detailCampAddr}">
            <c:set var="i" value="${ i+1 }" />
            <input type="hidden" name="detailCampAddr" value="${detailCampAddr}">
          </c:forEach>
          <c:forEach var="circumstance" items="${search.circumstance}">
              <c:set var="i" value="${ i+1 }" />
              <input type="hidden" name="circumstance" value="${circumstance}">
          </c:forEach>
          <c:forEach var="mainSite" items="${search.mainSite}">
              <c:set var="i" value="${ i+1 }" />
              <input type="hidden" name="mainSite" value="${mainSite}">
          </c:forEach>
          <c:forEach var="subSite" items="${search.subSite}">
              <c:set var="i" value="${ i+1 }" />
              <input type="hidden" name="subSite" value="${subSite}">
          </c:forEach>
          <c:forEach var="theme" items="${search.theme}">
              <c:set var="i" value="${ i+1 }" />
              <input type="hidden" name="theme" value="${theme}">
          </c:forEach>
          <c:forEach var="price" items="${search.price}">
              <c:set var="i" value="${ i+1 }" />
              <input type="hidden" name="price" value="${price}">
          </c:forEach>
          <input type="hidden" id="currentPage" name="currentPage" value="0"/>
        </form>
        
        <hr>
        
        <!-- 캠핑장 목록 시작-->   
        <form id="get_camp">
        <c:set var="i" value="0" />
           <c:forEach var="camp" items="${list}">
              <c:set var="i" value="${ i+1 }" />
                 <div class="row">
                    
                    <!-- 캠핑장 이미지 -->
                    <div class="col-lg-3 ">
                        <div class="image" name="campNo" data-campNo="${camp.campNo}"  style="cursor: pointer; width: 200px; height: 150px; border-radius: 10px; display: flex; justify-content: center; align-items: center">
                          <img src="/uploadfiles/campimg/campbusiness/camp/${camp.campImg1}" onerror="this.src='/uploadfiles/campimg/campbusiness/camp/no_image.jpg'"  alt="캠핑장 대표이미지" >
                        </div>                   
                     </div>            
                     
                     <!-- 캠핑장 정보 -->
                    <div class="col-lg-9">
                    
                       <div class="row">
                          <div class="col-xs-4 camp_name_sub" style="font-size: large; font-weight: bold ">${camp.user.campName}&nbsp;</div>
                          <div class="col-xs-4" style="margin-top: 3px;"> 등록일 : ${camp.campRegDate}</div>
                       </div>   
                       
                       <div class="row">
                        <br>
                         <div class="col-xs-2 substring" value="${camp.campRate}"></div>
                         <div class="col-xs-4"> 이달의 조회수 : ${camp.campViewCountCurrentMonth}</div>
                       </div>
                       
                       <div class="row">
                          <br>
                         <div class="col-xs-8" style="font-size: medium;"> 주소 : ${camp.user.addr}</div>
                         <div class="col-xs-4 phone_format" value="${camp.user.campCall}">전화번호 : ${camp.user.campCall}</div>
                       </div>
                       
                       <div class="row">
                        <br>
                         <div class="col-xs-12"> ${camp.campSummery}</div>
                       </div>
                                                      
                     </div>       
                     
                 </div>

                 <hr>
        </c:forEach>
        <!-- 캠핑장 목록 끝-->   
      </form>
      </div>
      
    </div>
         <!-- PageNavigation -->
         <div class="row">
           <jsp:include page="../common/pageNavigator.jsp"/>
        </div>


        <jsp:include page="../../view/common/footer.jsp"/>
  

        <script src="../../resources/lib/modals/examples.modals.js"></script>
        <script src="../../resources/js/campSearch.js"></script>

        <script type="text/javascript">

          window.onload = function() {

            $(document).scrollTop($(".container")[0].scrollHeight);

          }

          $(".phone_format").each(function(index,obj){
            
              let str = $(this).attr("value");
              let phone = str.replace(/(^02.{0}|^01.{1}|[0-9]{3})([0-9]+)([0-9]{4})/,"$1-$2-$3");
              return $(this).html("전화번호 : "+phone);
            
          });
              
          var currentPage = 1;
      
          function fncGetList(currentPage) {
            $("#currentPage").val(currentPage)
            $("#pagenavi").attr("method","POST").attr("action","/campGeneral/listCamp").submit();
          }
          
          $( function() {

            $(".substring").each(function(index,obj){
              
              let str = $(this).attr("value");
              let substring = str.substring(0, 3);
              return $(this).html("평점 : "+substring);
            
            });
    
            $(".order").on("click" , function() {

                  let sortCondition = $(this).attr("data");
                  console.log("소트컨디션 :: "+sortCondition);
                  $("#sortCondition").val(sortCondition);

                  $("#order").attr("method","POST").attr("action","/campGeneral/listCamp").submit();
              });
         
            $(  ".image"  ).on("click", function() {    
                var campNo = $(this).data("campno");
                console.log(campNo);
                $("#get_camp").attr("method","POST").attr("action","/campGeneral/getCamp?campNo="+campNo).submit()
            });   
    
          });
        </script>
  </body>
</html>
