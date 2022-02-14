package site.gamsung.service.camp.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.shared.invoker.SystemOutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import site.gamsung.service.camp.CampRatingReviewDAO;
import site.gamsung.service.camp.CampSearchDAO;
import site.gamsung.service.common.RatingReviewService;
import site.gamsung.service.common.Search;
import site.gamsung.service.domain.Camp;
import site.gamsung.service.domain.RatingReview;

@Service("campRatingReviewServiceImpl")
public class CampRatingReviewServiceImpl implements RatingReviewService{

	@Autowired
	@Qualifier("campRatingReviewDAOImpl")
	private CampRatingReviewDAO campRatingReviewDAO;
	
	@Autowired
	@Qualifier("campSearchDAOImpl")
	private CampSearchDAO campSearchDAO;
	
	public void setCampRatingReviewDAO(CampRatingReviewDAO campRatingReviewDAO) {
		this.campRatingReviewDAO = campRatingReviewDAO;
	}

	public CampRatingReviewServiceImpl() {
		System.out.println(this.getClass());
	}
	

	//평점&리뷰 등록 Service - 등록 시 캠핑장 평균평점이 변경 되야 하므로 등록한 평점&리뷰를 포함한 평점을 계산해서 update해줌 
	@Override
	public void addRatingReview(RatingReview ratingReview) {
		
		//평점&리뷰 DB에 등록
		campRatingReviewDAO.addCampRatingReview(ratingReview);
		
		//DB에 등록된 캠핑장 평점을 가져오기위한 캠핑장 등록번호
		int campNo = ratingReview.getCamp().getCampNo();
		
		//DB에 등록된 평점을 select하여 List에 담음
		List<Double> ratingList = campRatingReviewDAO.getCampRating(campNo);
		
		//계산 후 캠핑장 평균 평점을 담을 변수 선언 및 초기화
		double avgRating = 0;
		
		//평점들을 반복문으로 더함
		for (Double rl : ratingList) {
			avgRating += rl;
		}
		
		//더한 평점을 List 크기만큼 나누어서 평균평점을 구함
		avgRating /= ratingList.size();
		
		//캠핑장 등록번호와 계산된 평균 평점을 Map에 담고 DB에 Update함
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("campNo", campNo);
		map.put("avgRating", avgRating);
		
		campRatingReviewDAO.updateCampAvgRating(map);
	}

	@Override
	public Map<String, Object> listRatingReview(Search search) {
		
		List<RatingReview> list = new ArrayList<RatingReview>();
		Map<String, Object> map = new HashMap<String, Object>();
		
		if(search.getCampNo() != 0) {
			list = campRatingReviewDAO.listCampRatingReview(search);
			Camp camp = campSearchDAO.getCamp(search.getCampNo());
			double campRating = camp.getCampRate();
			map.put("campRating", campRating);
			
		}else {
			System.out.println("서치 조건 :: "+search);
			list = campRatingReviewDAO.listMyRatingReview(search);
		}
		
		int totalCount = campRatingReviewDAO.getTotalCount(search);
				
		map.put("list", list);
		map.put("totalCount", totalCount);
				
		return map;
	}
	
	@Override
	public void updateRatingReview(RatingReview ratingReview) {
		campRatingReviewDAO.updateCampRatingReview(ratingReview);
	}
		
	@Override
	public void deleteRatingReview(RatingReview ratingReview) {
		campRatingReviewDAO.deleteCampRatingReviewComment(ratingReview);
	}

	@Override
	public RatingReview getRatingReview(int ratingReviewNo) {
		return campRatingReviewDAO.getCampRatingReview(ratingReviewNo);
	}
	
}
