package site.gamsung.controller.camp;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import site.gamsung.service.camp.CampReservationService;
import site.gamsung.service.camp.CampSearchService;
import site.gamsung.service.common.Page;
import site.gamsung.service.common.RatingReviewService;
import site.gamsung.service.common.Search;
import site.gamsung.service.domain.Camp;
import site.gamsung.service.domain.CampReservation;
import site.gamsung.service.domain.MainSite;
import site.gamsung.service.domain.Notice;
import site.gamsung.service.domain.NoticeWrapper;
import site.gamsung.service.domain.Payment;
import site.gamsung.service.domain.RatingReview;
import site.gamsung.service.domain.User;
import site.gamsung.service.servicecenter.NoticeService;

/*
캠핑장 관련 일반회원(비회원)이 접근 할수 있는 Service를 처리하는 Ctrl
검색, 예약, 리뷰, 공지사항 확인
작성자 : 박철홍
*/
@Controller
@RequestMapping("/campGeneral/*")
public class CampGeneralController {

	@Autowired
	@Qualifier("campSearchServiceImpl")
	private CampSearchService campSearchService;
	
	@Autowired
	@Qualifier("campReservationServiceImpl")
	private CampReservationService campReservationService;
	
	@Autowired
	@Qualifier("noticeServiceImpl")
	private NoticeService noticeService;
	
	@Autowired
	@Qualifier("campRatingReviewServiceImpl")
	private RatingReviewService ratingReviewService;
	
	public CampGeneralController() {
		System.out.println(this.getClass());
	}
	
	@Value("#{commonProperties['pageUnit']}")
	int pageUnit;	
	
	@Value("#{commonProperties['pageSize']}")
	int pageSize;
	
	@Value("#{commonProperties['campPageSize']}")
	int campPageSize;
	
	//검색 조건을 Search domain에 담아 캠핑장 검색 및 결과 페이지 구성을 위한 정보를 모델에 담고 listCamp.jsp로 forward
	@RequestMapping(value = "listCamp", method = RequestMethod.POST)
	public String listCamp(@ModelAttribute("search") Search search, Model model){
		System.out.println("/campGeneral/listCamp : POST");
		
		//최조 검색시 현재 페이지값이 없을경우 1로 셋팅
		if (search.getCurrentPage() == 0) {
			search.setCurrentPage(1);
		}
		
		search.setPageSize(pageSize);
		
		//Service 로직 수행 결과로 서치된 캠핑장 정보와 총 서치된 캠핑장 갯수를 map에 담음
		Map<String, Object> map = campSearchService.listCamp(search);
		
		//현재 페이지, 서치된 캠핑장 갯수, 페이지 숫자, 페이지당 노출 캠핑장 갯수 정보로 결과 페이지를 구성하는 Page 생성
		Page resultPage = new Page(search.getCurrentPage(), ((Integer) map.get("totalCount")).intValue(), pageUnit, pageSize);
		
		//서치된 캠핑장 정보, 결과 Page, search 조건(네비게이션 및 소팅시에 검색 조건 정보가 있어야함)을 모델에 담음
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		
		return "forward:/view/camp/listCamp.jsp";
	}
	
	//캠핑장 등록 번호를 GET 방식으로 받아 캠핑장 정보(기본정보, 주요시설정보, 부가시설정보)를 검색 후 모델에 담고 getCamp.jsp로 forward 
	@RequestMapping(value = "getCamp", method = RequestMethod.GET)
	public String getCamp(@RequestParam("campNo") int campNo,  Model model ){
		System.out.println("/campGeneral/getCamp : GET");
		
		//Service 로직 수행 결과를 map에 담음
		Map<String, Object> map = campSearchService.getCamp(campNo);
		
		//map에 담긴 캠핑장 기본정보, 주요시설정보, 부가시설정보를 모델에 담음
		model.addAttribute("camp", map.get("camp"));
		model.addAttribute("mainSite", map.get("mainSite"));
		model.addAttribute("subSite", map.get("subSite"));
		model.addAttribute("mainSiteType", map.get("mainSiteType"));
				
		return "forward:/view/camp/getCamp.jsp";
	}
	
	//예약 진행을 위한 정보를 Post방식으로 받아 단계별로 예약 진행
	@RequestMapping(value = "addReservation", method = RequestMethod.POST)
	public String addReservation(@RequestParam("mainSiteNo") int mainSiteNo, Model model, 
									@ModelAttribute("campReservation") CampReservation campReservation, HttpSession httpSession){
		
		System.out.println("/campGeneral/addReservation : POST");
		
		//로그인시 세션에 등록된 user 정보를 가져옴 
		User user = (User)httpSession.getAttribute("user");
		
		//선택한 주요시설 등록번호를 예약정보에 등록
		MainSite mainSite = new MainSite();
		mainSite.setMainSiteNo(mainSiteNo);
		campReservation.setMainSite(mainSite);
		
		if(user == null) {
			
			return "redirect:/";
		
		//주요시설이 선택되지 않았을 경우(0일경우) 예약 진행 시작 전이므로 기본적인 예약정보를 가지고 addReservationFirst.jsp로 forward
		}else if(mainSiteNo == 0) {
			
			model.addAttribute("campReservation",campReservation);
			
			return "forward:/view/camp/addReservationFirst.jsp";

		//주요시설 등록번호가 0이 아니고 이용인원이 선택되지 않았을경우(0일경우) 선택된 주요시설 정보와 예약정보를 가지고 addReservationSecond.jsp로 forward
		}else if(campReservation.getUseNum() == 0){
			
			MainSite resultMainSite = campSearchService.getMainSite(campReservation);
			Camp camp = campSearchService.getCampByReservation(campReservation.getCamp().getCampNo());
			model.addAttribute("campReservation",campReservation);
			model.addAttribute("mainSite",resultMainSite);
			model.addAttribute("camp",camp);
			
			return "forward:/view/camp/addReservationSecond.jsp";
		
		//주요시설 등록번호가 0이아니고 이용인원이 0이 아닐경우 선택된 주요시설 정보와 예약정보를 가지고 addReservationThird.jsp로 forward
		}else{
			
			MainSite resultMainSite = campSearchService.getMainSite(campReservation);
			model.addAttribute("campReservation",campReservation);
			model.addAttribute("mainSite",resultMainSite);
						
			return "forward:/view/camp/addReservationThird.jsp";
		}
	}
	
	//3단계까지 예약 진행 후 결제 페이지로 넘어가기전 결제 담당 조원의 요청으로 캠핑장예약 정보 및 결제 정보를 map에 담아 payment Controller로 forward
	@RequestMapping(value = "addPayment", method = RequestMethod.POST)
	public String addPayment(@RequestParam("mainSiteNo") int mainSiteNo,
			@ModelAttribute("campReservation") CampReservation campReservation,
			HttpSession httpSession, HttpServletRequest request, Model model){
		
		//로그인시 세션에 등록된 user 정보를 가져옴
		User user = (User)httpSession.getAttribute("user");
		
		MainSite mainSite = new MainSite();
		mainSite.setMainSiteNo(mainSiteNo);
		campReservation.setMainSite(mainSite);
		
		if(user == null) {
			
			return "redirect:/";
		
		//결제 담당조원의 요청 사항 : 예약등록번호 필요, 결제 정보 필요
		//예약 테이블에 임시등록 컬럼을 추가 해 예약 결제전 임시로 테이블에 insert 후 auto increment된 예약등록번호 획득
		//임시등록시 주요시설에도 예약 선점 상태로 등록됨 마이페이지에서 결제대기 확인 후 결제 가능
		//임시등록 된 예약은 당일 결제가 완료 되지 않을 경우 삭제(실제 지우진 않고 delete flag처리)하고 주요시설 예약선점 지우는 스케줄러 작동
		//원하는 결제 정보를 담은 Payment 생성 후 map에 담아서 Payment Controller로 forward 
		} else {
			
			campReservation.setUser(user);
			campReservationService.updateMainSiteTemp(campReservation);
			campReservation = campReservationService.addTempReservation(campReservation);
			campReservation.setUser(user);
			
			Payment payment = new Payment();
			payment.setPaymentSender(campReservation.getUser().getId());
			payment.setPaymentReceiver(campReservation.getCamp().getUser().getId());
			payment.setPaymentCode("R1");
			payment.setPaymentPriceTotal(campReservation.getTotalPaymentPrice());
			
			Map<String, Object> payCampMap = new HashMap<String, Object>();
			payCampMap.put("payment", payment);
			payCampMap.put("campReservation", campReservation);
			
			request.setAttribute("payCampMap", payCampMap);
			
			return "forward:/payment/readyPayment";
			
		}
		
	}
	
	@RequestMapping(value = "addPaymentByMyPage", method = RequestMethod.POST)
	public String addPaymentByMyPage(@ModelAttribute("campReservation") CampReservation campReservation,  
										HttpServletRequest request, HttpSession httpSession, Model model){
		System.out.println("/campGeneral/addPaymentByMyPage : POST");
		
		User user = (User)httpSession.getAttribute("user");
		
		campReservation.setUser(user);
		campReservation = campReservationService.getReservationByPayment(campReservation);
		campReservation.setUser(user);
		
		Payment payment = new Payment();
		payment.setPaymentSender(campReservation.getUser().getId());
		payment.setPaymentReceiver(campReservation.getCamp().getUser().getId());
		payment.setPaymentCode("R1");
		payment.setPaymentPriceTotal(campReservation.getTotalPaymentPrice());
		
		Map<String, Object> payCampMap = new HashMap<String, Object>();
		payCampMap.put("payment", payment);
		payCampMap.put("campReservation", campReservation);
		
		request.setAttribute("payCampMap", payCampMap);
					
		return "forward:/payment/readyPayment";
	}
		
	@RequestMapping(value = "listMyReservation" )
	public String listMyReservation(@ModelAttribute("search") Search search, Model model ,HttpSession httpSession) throws Exception{
		System.out.println("/campGeneral/listMyReservation : GET / POST");
			
		User user = (User)httpSession.getAttribute("user");
		
		if (search.getCurrentPage() == 0) {
			search.setCurrentPage(1);
		}
		
		search.setPageSize(pageSize);
		
		if(user == null) {
			
			return "redirect:/";
			
		} else {
			
			Map<String, Object> map = campReservationService.listMyReservation(search, user.getId());
			
			Page resultPage = new Page(search.getCurrentPage(), ((Integer) map.get("totalCount")).intValue(), pageUnit, pageSize);
						
			model.addAttribute("list", map.get("list"));
			model.addAttribute("resultPage", resultPage);
			model.addAttribute("search", search);
			model.addAttribute("user", user);

			return "forward:/view/camp/listMyReservation.jsp";
		}
	}
	
	@RequestMapping(value = "getMyReservation", method = RequestMethod.GET)
	public String getMyReservation(@RequestParam String reservationNo, Model model){
		
		System.out.println("/campGeneral/geteMyReservation : GET");
			
		CampReservation campReservation = campReservationService.getReservation(reservationNo);
	
		model.addAttribute("campReservation" , campReservation);
		
		return "forward:/view/camp/getMyReservation.jsp";
	}
	
	@RequestMapping(value = "updateMyReservationView", method = RequestMethod.GET)
	public String updateMyReservationView(@RequestParam String reservationNo, Model model){
		System.out.println("/campGeneral/updateMyReservationView : GET");
		
		CampReservation campReservation = campReservationService.getReservation(reservationNo);
		
		model.addAttribute("campReservation" , campReservation);
		
		return "forward:/view/camp/updateMyReservation.jsp";
	}
	
	@RequestMapping(value = "updateMyReservation", method = RequestMethod.POST)
	public String updateMyReservation(@ModelAttribute CampReservation campReservation,
									HttpServletRequest request, HttpSession httpSession, Model model){
		
		System.out.println("/campGeneral/updateMyReservation : POST");
		
		if(campReservation.getTotalPaymentPrice() == 0) {
			//예약 테이블 정보 변경
			campReservation.setReservationStatus(2);
			campReservationService.updateReservation(campReservation);
			campReservation = campReservationService.getReservation(campReservation.getReservationNo());
			
			model.addAttribute("campReservation" , campReservation);
			
			return "forward:/view/camp/getMyReservation.jsp";
			
		}else if(campReservation.getTotalPaymentPrice() > 0) {
			//예약 결제 후 예약 테이블 정보 변경
			User user = (User)httpSession.getAttribute("user");
			campReservation.setUser(user);
			campReservation.setReservationStatus(2);
			
			CampReservation campReservationByCurrent = campReservationService.getCampIdByAppendPayment(campReservation);
			
			Camp camp = new Camp();
			camp.setCampImg1(campReservationByCurrent.getCamp().getCampImg1());
	
			User campUser = new User();
			campUser.setCampName(campReservationByCurrent.getCamp().getUser().getCampName());
			camp.setUser(campUser);
			
			campReservation.setCamp(camp);
			
			MainSite mainSite = new MainSite();
			mainSite.setMainSiteType(campReservationByCurrent.getMainSite().getMainSiteType());
			mainSite.setMainSiteNo(campReservationByCurrent.getMainSite().getMainSiteNo());
			
			System.out.println(mainSite);
			
			campReservation.setMainSite(mainSite);
			campReservation.setReservationRegDate(campReservationByCurrent.getReservationRegDate());

			Payment payment = new Payment();
			payment.setPaymentSender(campReservation.getUser().getId());
			payment.setPaymentReceiver(campReservationByCurrent.getCamp().getUser().getId());
			payment.setPaymentCode("R2");
			payment.setPaymentPriceTotal(campReservation.getTotalPaymentPrice());
			
			Map<String, Object> payCampMap = new HashMap<String, Object>();
			payCampMap.put("payment", payment);
			payCampMap.put("campReservation", campReservation);
			
			request.setAttribute("payCampMap", payCampMap);
			
			return "forward:/payment/readyPayment";
			
		}else {
			return null;
		}

	}
	
	//캠핑장 등록번호와 검색 조건(정렬, 페이지 네비게이션)을 GET 또는 POST 방식으로 받아 검색 후 모델에 담고 listRatingReview.jsp로 forward
	//캠핑장 상세보기 시에 getCamp와 같이 실행(iframe에서 GET 방식으로)
	@RequestMapping(value = "listCampRatingReview")
	public String listCampRatingReview(@RequestParam("campNo") int campNo , @ModelAttribute("search") Search search , Model model){
	
		System.out.println("/campGeneral/listCampRatingReview : GET / POST");	
			
		if (search.getCurrentPage() == 0) {
			search.setCurrentPage(1);	
		}
		
		search.setPageSize(campPageSize);
		search.setCampNo(campNo);
		
		Map<String, Object> map = ratingReviewService.listRatingReview(search);
		
		Page resultPage = new Page(search.getCurrentPage(), ((Integer) map.get("totalCount")).intValue(), pageUnit, campPageSize);
		
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		model.addAttribute("campRating", map.get("campRating"));
		model.addAttribute("campNo", campNo);
		model.addAttribute("type", "CAMP");
		
		return "forward:/view/camp/listRatingReview.jsp";
	}
	
	@RequestMapping(value = "listBusinessCampRatingReview")
	public String listBusinessCampRatingReview(@RequestParam("campNo") int campNo , @ModelAttribute("search") Search search , Model model){
	
		System.out.println("/campGeneral/listBusinessCampRatingReview : GET / POST");	

		if (search.getCurrentPage() == 0) {
			search.setCurrentPage(1);	
		}
		
		search.setPageSize(campPageSize);
		search.setCampNo(campNo);
		
		Map<String, Object> map = ratingReviewService.listRatingReview(search);
		
		Page resultPage = new Page(search.getCurrentPage(), ((Integer) map.get("totalCount")).intValue(), pageUnit, campPageSize);
				
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		model.addAttribute("campRating", map.get("campRating"));
		model.addAttribute("campNo", campNo);
		model.addAttribute("type", "CAMP");
		
		return "forward:/view/camp/listCampRatingReview.jsp";
	}
	
	@RequestMapping(value = "listCampNotice")
	public String listCampNotice(@ModelAttribute("search") Search search , Model model ){

		System.out.println("/campGeneral/listCampNotice : GET / POST");
		
		if (search.getCurrentPage() == 0) {
			search.setCurrentPage(1);
		}
		
		search.setPageSize(pageSize);
		
		try {
			NoticeWrapper noticeWrapper = noticeService.listNotice(search);
			Page resultPage = new Page(search.getCurrentPage(), noticeWrapper.getTotalCount(), pageUnit, pageSize);
			
			model.addAttribute("wrapper", noticeWrapper);
			model.addAttribute("resultPage", resultPage);
			model.addAttribute("search", search);
			model.addAttribute("campNo", search.getCampNo());
	
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		return "forward:/view/camp/listCampNotice.jsp";
	
	}
	
	@RequestMapping(value = "getCampNotice")
	public String getcampNotice(@RequestParam int noticeNo, Model model){
		System.out.println("/campGeneral/getCampNotice : GET");
		
		try {
			noticeService.updateViewCount(noticeNo);
			Notice notice = noticeService.getNotice(noticeNo);

			model.addAttribute("notice" , notice);
			model.addAttribute("noticeType", "get");
		}catch (Exception e) {
			e.printStackTrace();
		}
				
		return "forward:/view/camp/getCampNotice.jsp";
	}
	
	@RequestMapping(value = "addCampRatingReviewView", method = RequestMethod.GET)
	public String addCampRatingReviewView(@ModelAttribute("reservationNo") String reservationNo, Model model ,HttpSession httpSession){
		System.out.println("/campGeneral/addCampRatingReviewView : GET");
		
		CampReservation campReservation = campReservationService.getReservation(reservationNo);
		Camp camp = campSearchService.getCampByReservation(campReservation.getCamp().getCampNo());
		
		model.addAttribute("campReservation", campReservation);
		model.addAttribute("camp", camp);
		
		return "forward:/view/camp/addCampRatingReview.jsp";
	}
	
	//리뷰 정보를 POST방식으로 받아 DB에 등록 이후 예약 상태는 리뷰 등록 완료로 전환 하고 나의 리뷰 목록으로 redirect
	@RequestMapping(value = "addCampRatingReview", method = RequestMethod.POST)
	public String addCampRatingReview(@ModelAttribute("RatingReview") RatingReview ratingReview, @RequestParam("article_file") MultipartFile[] reviewImg, 
											@RequestParam("reservationNo") String reservationNo, Model model ,HttpSession httpSession){
		
		System.out.println("/campGeneral/addCampRatingReview : POST");
		
		//MultipartFile Upload시 원하는 column에 insert하기 위한 index(최대 3장까지 가능)
		int	index = 1;
		
		//MultipartFile[]로 받은 reviewImg를 하나 하나 뽑아 랜덤한 이름으로 바꾸고 원하는 file 경로와 column에 insert하기 위한 반복문
		for(MultipartFile multpartfile: reviewImg) {
				
		//MultipartFile로 받은 reviewImg에서 file이름을 originalReviewImg 넣는다. 
		String originalReviewImg = multpartfile.getOriginalFilename(); 
		
	    if(originalReviewImg != null && originalReviewImg != "") {
	    
				//그 파일명 .의 인덱스 숫자까지 잘라서 확장자만 추출 (ex .jsp)
				String originalFileExtension = originalReviewImg.substring(originalReviewImg.lastIndexOf("."));
				
				// UUID로 랜덤하게 생성한거에 -가 있으면 없애고 확장자를 붙임 (ex 359498a2ff1a40b8a8e16f6c43dd2bf3.jpg)
				String path = httpSession.getServletContext().getRealPath("/");
				String attach_path = "uploadfiles/campimg/review/";
			    String storedFileName = UUID.randomUUID().toString().replaceAll("-", "") + originalFileExtension;
			    
			    //File을 생성해서 주소랑 새로 만든 파일이름을 넣는다. 
			    File file = new File(path + attach_path + storedFileName);	
			    
			    System.out.println("file::::"+file);
			    
				try {
					multpartfile.transferTo(file); // postImg를 transferto(보낸다)file로
				} catch (Exception e) {
					
					e.printStackTrace();
				}
			    
				if (index == 1) {
					ratingReview.setImg1(storedFileName);
				} else if (index == 2 ) {
					ratingReview.setImg2(storedFileName);
				} else {
					ratingReview.setImg3(storedFileName);        
				}
			
				index ++;
			}
		}
		
		//Session에서 유저 정보 가져와 입력
		User user = (User)httpSession.getAttribute("user");
		ratingReview.setUser(user);
		
		//리뷰 평점이 커뮤니티 게시판에서 등록한 것인지 이용 완료 후기 인지 판단하기 위한 리뷰 상태값
		if(ratingReview.getRatingReviewStatus() == 2) {
			ratingReview.setRatingReviewStatus(2);	
		}else {
		ratingReview.setRatingReviewStatus(1);
		}
		
		//리뷰가 등록 되면 예약상태는 이용완료에서 리뷰등록완료로 전환됨을 확인하기위한 예약상태 업데이트(이용금액은 update 시 append 되기 때문에 초기화함)
		CampReservation campReservation = campReservationService.getReservation(reservationNo);
		campReservation.setReservationStatus(7);
		campReservation.setTotalPaymentPrice(0);
				
		ratingReviewService.addRatingReview(ratingReview);
		campReservationService.updateReservationStatus(campReservation);
		
		return "redirect:/view/camp/listMyRatingReview.jsp";
	}
	
	@RequestMapping(value = "listMyCampRatingReview")
	public String listMyCampRatingReview(@ModelAttribute("search") Search search, Model model ,HttpSession httpSession){
		System.out.println("/campGeneral/listMyCampRatingReview : GET / POST");
		
		User user = (User)httpSession.getAttribute("user");
		
		if (search.getCurrentPage() == 0) {
			search.setCurrentPage(1);
		}
		
		search.setPageSize(campPageSize);
		
		if(user == null) {
			
			return "redirect:/";
			
		} else {
			
			search.setId(user.getId());
			Map<String, Object> map = ratingReviewService.listRatingReview(search);
			
			Page resultPage = new Page(search.getCurrentPage(), ((Integer) map.get("totalCount")).intValue(), pageUnit, campPageSize);
			System.out.println(resultPage);
			
			model.addAttribute("list", map.get("list"));
			model.addAttribute("resultPage", resultPage);
			model.addAttribute("search", search);
			model.addAttribute("user", user);
		
			return "forward:/view/camp/listMyRatingReview.jsp";
		}
		
	}
	
	@RequestMapping(value = "deleteMyCampRatingReview", method = RequestMethod.GET)
	public String deleteMyCampRatingReview(@RequestParam int ratingReviewNo, Model model, HttpSession httpSession){
		System.out.println("/campGeneral/deleteMyCampRatingReview : GET");
		
		RatingReview ratingReview = new RatingReview();
		
		ratingReview.setRatingReviewNo(ratingReviewNo);
		ratingReview.setReviewDeleteFlag("Y");
		
		ratingReviewService.updateRatingReview(ratingReview);
		
		User user = (User)httpSession.getAttribute("user");
		Search search = new Search();
		
		if (search.getCurrentPage() == 0) {
			search.setCurrentPage(1);
		}
		
		search.setPageSize(campPageSize);
		search.setId(user.getId());
		Map<String, Object> map = ratingReviewService.listRatingReview(search);
		
		Page resultPage = new Page(search.getCurrentPage(), ((Integer) map.get("totalCount")).intValue(), pageUnit, campPageSize);
			
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		model.addAttribute("user", user);
	
		return "forward:/view/camp/listMyRatingReview.jsp";
		
	}

}
