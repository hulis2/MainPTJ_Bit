package site.gamsung.service.camp.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import site.gamsung.service.camp.CampSearchDAO;
import site.gamsung.service.camp.CampSearchService;
import site.gamsung.service.common.Search;
import site.gamsung.service.domain.Camp;
import site.gamsung.service.domain.CampReservation;
import site.gamsung.service.domain.MainSite;
import site.gamsung.service.domain.SubSite;

/*
캠핑장 검색을 위한 서비스 CampSearchService interface를 구현함
캠핑장 검색 결과, 캠핑장 상세보기 결과, 메인화면 추천 캠핑장 검색 기능
작성자 : 박철홍
*/
@Service("campSearchServiceImpl")
@EnableTransactionManagement
public class CampSearchServiceImpl implements CampSearchService{
	
	@Autowired
	@Qualifier("campSearchDAOImpl")
	private CampSearchDAO campSearchDAO;

	public void setCampSearchDAO(CampSearchDAO campSearchDAO) {
		this.campSearchDAO = campSearchDAO;
	}

	public CampSearchServiceImpl() {
		System.out.println(this.getClass());
	}

	//검색 조건에 맞는 캠핑장 정보와 서치된 총 캠핑장 갯수 구하는 Service
	@Override
	public Map<String, Object> listCamp(Search search){
		
		List<Camp> list = campSearchDAO.listCamp(search);
		int totalCount = campSearchDAO.getTotalCount(search);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("list", list);
		map.put("totalCount", new Integer(totalCount));
	
		return map;
	}

	//캠핑장 상세정보 검색 Service - 검색시 조회수 증가, 캠핑장 정보, 주요시설 정보, 부가시설 정보 DAO에 접근
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public Map<String, Object> getCamp(int campNo){
		
		//조회수 증가 DAO
		campSearchDAO.updateViewCount(campNo);
		
		Camp camp = campSearchDAO.getCamp(campNo);
		List<MainSite> mainSite = campSearchDAO.getMainSite(campNo);
		List<SubSite> subSite = campSearchDAO.getSubSite(campNo);
		
		//화면에 주요시설 정보 노출 시 주요시설 타입 중복 제거를 위한 List
		List<String> mainSiteType = new ArrayList<String>();
		
		//제일 처음 주요시설타입 리스트에 등록
		mainSiteType.add(mainSite.get(0).getMainSiteType());
		
		//반복문을 통해 리스트 안에 중복된 주요시설 타입이 있을때는 등록을 안하고 없을때 등록함
		for (int i = 0; i < mainSite.size()-1; i++) {
			
			boolean k = true;
			
			for (int j = 0; j < mainSiteType.size(); j++) {
				if(mainSite.get(i).getMainSiteType().equals(mainSiteType.get(j))){
					k = false;
				}
			}
			
			if(k) {
				mainSiteType.add(mainSite.get(i).getMainSiteType());
			}
		}
						
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("camp", camp);
		map.put("mainSite", mainSite);
		map.put("subSite", subSite);
		map.put("mainSiteType", mainSiteType);
		
		return map;
	}
	
	@Override
	public Camp getCampByReservation(int campNo) {
		return campSearchDAO.getCamp(campNo);
	}

	@Override
	public MainSite getMainSite(CampReservation campReservation) {
		return campSearchDAO.getMainsiteByReservation(campReservation);
	}

	@Override
	public Map<String, Object> getTopCamp(){
		
		Camp topRating = campSearchDAO.getTopRatingCamp();
		Camp topView = campSearchDAO.getTopViewCamp();
		Camp topReservation = campSearchDAO.getTopReservationCamp();
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("topRating", topRating);
		map.put("topView", topView);
		map.put("topReservation", topReservation);
		
		return map;
	}

}
