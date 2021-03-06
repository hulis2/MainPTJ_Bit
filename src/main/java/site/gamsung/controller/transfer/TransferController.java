package site.gamsung.controller.transfer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import site.gamsung.service.camp.CampReservationService;
import site.gamsung.service.common.Page;
import site.gamsung.service.common.Search;
import site.gamsung.service.community.CommunityService;
import site.gamsung.service.domain.Camp;
import site.gamsung.service.domain.CampReservation;
import site.gamsung.service.domain.Payment;
import site.gamsung.service.domain.Post;
import site.gamsung.service.domain.Receive;
import site.gamsung.service.domain.Transfer;
import site.gamsung.service.domain.User;
import site.gamsung.service.payment.PaymentService;
import site.gamsung.service.transfer.ReceiveService;
import site.gamsung.service.transfer.TransferService;
import site.gamsung.service.user.UserService;


@RequestMapping("/transfer/*")
@Controller
public class TransferController {
   
   
   @Autowired
   @Qualifier("transferServiceImpl")
   private TransferService transferService;   

   @Autowired
   @Qualifier("receiveServiceImpl")
   private ReceiveService receiveService;   
   


   @Autowired
   @Qualifier("paymentServiceImpl")
   private PaymentService paymentService;
   
   @Autowired
   @Qualifier("userServiceImpl")
   private UserService userService;  

   
   
   
   
   @Autowired
   @Qualifier("campReservationServiceImpl")
   private CampReservationService campReservationService;   
   
   @Value("#{commonProperties['pageUnit']}")
   int pageUnit;
   
   @Value("#{commonProperties['pageSize']}")
   int pageSize;
   

   public TransferController() {
      System.out.println(this.getClass());
   }

   // ?????? ?????? ?????? ????????? navigation
   
   @RequestMapping(value = "addTransfer", method = RequestMethod.GET)   
   public String addTransfer(HttpSession session, Model model) {

      System.out.println("addTransfer Start");

      // ???????????? ?????? ????????? ????????? ????????? ????????????.
      User user = (User) session.getAttribute("user");

      if (user == null) {
         return "redirect:/";
      }
            
       Search search = new Search();
       String id = user.getId();      
       
       if (search.getCurrentPage() == 0) {
            search.setCurrentPage(1);
         }
         
      search.setPageSize(10);
              
       Map<String, Object> List   = campReservationService.listMyReservation(search, id);

       
      List<CampReservation> list = (List<CampReservation>) List.get("list");
      
      System.out.println("list::::::::::::::::::::::::::"+list);
      System.out.println("list:::::::::::::::::???");
         
      model.addAttribute("userId", user.getId());
      model.addAttribute("list", list);

      return "forward:/view/transfer/addTransfer.jsp";
   }
   
   
   // ?????????????????? Mapping

   @RequestMapping(value = "addTransfer", method = RequestMethod.POST) // RequestParam??? ????????? file type????????? name??? ?????????.
   public String addTransfer(@ModelAttribute("transfer") Transfer transfer, MultipartFile[] paymentImgs,
         HttpServletRequest req, HttpSession session, Model model) throws Exception {

      System.out.println("addTransfer Post Start");
      System.out.println("transfer:::::::"+transfer);
      
         int index = 1;
         
//         ArrayList<String> imgs = new ArrayList<String>();
         
         for (MultipartFile multpartfile : paymentImgs) {

            // MultipartFile??? ?????? postImg?????? file????????? originalPostImg??? ?????????.
            String originalPostImg = multpartfile.getOriginalFilename();

            System.out.println("originalPostImg::::" + originalPostImg + "!");

            if (originalPostImg != null && originalPostImg != "") {

               // ??? ????????? .??? ????????? ???????????? ????????? ???????????? ?????? (ex .jsp)
               String originalFileExtension = originalPostImg.substring(originalPostImg.lastIndexOf("."));

               // UUID??? ???????????? ??????????????? -??? ????????? ????????? ???????????? ?????? (ex 359498a2ff1a40b8a8e16f6c43dd2bf3.jpg)
               String root_path = req.getSession().getServletContext().getRealPath("/");
               String attach_path = "uploadfiles/transfer/";
               String storedFileName = UUID.randomUUID().toString().replaceAll("-", "") + originalFileExtension;

               System.out.println(root_path);
               // File??? ???????????? ????????? ?????? ?????? ??????????????? ?????????.
               File file = new File(root_path + attach_path + storedFileName);

               System.out.println("file::::" + file);

               // MultipartFile.transferTo(File file) - Byte????????? ???????????? File????????? ????????? ?????? ????????? ????????????.
               // file?????? ????????? ???????????? ??????????????? ??????. ?????? PostImg??? ?????????.
               multpartfile.transferTo(file); // postImg??? transferto(?????????)file???

               System.out.println("file");
               System.out.println("file.getPath::" + file.getPath());

//               imgs.add(storedFileName);
            
               if (index == 1) {
                  transfer.setPaymentImg(storedFileName);
               } else if (index == 2) {
                  transfer.setPaymentImg(storedFileName);
               } else {
                  transfer.setPaymentImg(storedFileName);
               }

               index++;

            } // originalPostImg if??? END

         } // postImg for??? END

//         transfer.setPaymentImg(imgs);
         
         User user = (User) session.getAttribute("user");
         
         
         
         transfer.setTransferOr(user);

         transferService.addTransfer(transfer);

//      ???????????? ??? ??????????????????. 
         
      return "redirect:listTransfer";
   }// ?????? method ??????
   
   
   
   // ???????????? ?????? ????????? navigation   
   @RequestMapping(value = "listTransfer")   
   public String listTransfer(@ModelAttribute("search") Search search, HttpSession session, Model model) throws Exception {

      System.out.println("listTransfer Start");

      User user = (User) session.getAttribute("user");

      if (user == null) {
         return "redirect:/";
      }
      
       Map<String, Object> map = new HashMap<String, Object>();
       
//       Search search = new Search();
           
       if (search.getCurrentPage() == 0) {
            search.setCurrentPage(1);
         }
             
      search.setPageSize(10);

      map.put("search", search);
             
      map = transferService.listTransfer(map);
      
      int TotalCount = (int) map.get("TotalCount");
   
      List<Transfer> list =  (List<Transfer>) map.get("list");
      
      Page resultPage = new Page( search.getCurrentPage(), TotalCount, pageUnit, pageSize);
            
      model.addAttribute("user", user);
      model.addAttribute("list", list);
      model.addAttribute("resultPage", resultPage);      
      
      return "forward:/view/transfer/listTransfer.jsp";
   }
   
   
   
   // ?????? ?????? ????????? 
   
   @RequestMapping(value = "getTransfer")   
   public String getTransfer(@RequestParam("transferNo") int transferNo, HttpSession session, Model model, Search search) throws Exception {

      System.out.println("getTransfer Start");

      User user = (User) session.getAttribute("user");

      if (user == null) {
         return "redirect:/";
      }
      
      System.out.println("???????????? ????????? transferNo:::::::"+transferNo);
               
      Transfer transfer = transferService.getTransfer(transferNo);
       
      System.out.println("DB?????? ????????? getTransfer:::"+transfer);
      
      System.out.println("getTransfer?????? TransferNO:::"+transferNo);
              
        search.setRole(user.getRole());
        search.setTransferNo(transferNo);
        search.setId(user.getId());
        search.setCurrentPage(1);
        search.setPageSize(10);
        System.out.println("StartRowNum:::::"+search.getStartRowNum());
        System.out.println("EndRowNum:::::"+search.getEndRowNum());
        
        
        int StartRowNum = search.getStartRowNum();
        int EndRowNum = search.getEndRowNum();
        
        search.setStartRowNum(StartRowNum);
        search.setEndRowNum(EndRowNum);
           
         //search setting?????? ??????????????? ???????????? ????????? ???????????????. ????????? 10????????? ???????????????. 
        
        // ??? ???????????? ????????? ????????? ??? ??? ??? ??????. ??? ???????????? ?????? ??????????????? ????????? 
        
        System.out.println("receiveserive??? ????????? search???:::"+search);
        
        List<Receive> listreceive = receiveService.listReceive(search);   
        
        System.out.println("DB?????? ????????? listreceive:::::::::"+listreceive);
   
         model.addAttribute("user", user);
         model.addAttribute("transfer", transfer);
         model.addAttribute("listreceive", listreceive);
   
      return "forward:/view/transfer/getTransfer.jsp";
      
      
   }
   
   // ?????????????????? My ????????? navigation   1.?????? ?????? 
   @RequestMapping(value = "listMyTransfer")   
   public String listMyTransfer(HttpSession session, Model model, Search search , @RequestParam(value = "options", defaultValue = "1") int options) throws Exception {

      System.out.println("listMyTransfer Start");
      
   
      User user = (User) session.getAttribute("user");
      int TotalCount = 0;
      List<Transfer> Transferlist = null;
      if (user == null) {
         return "redirect:/";
      }
      
       Map<String, Object> map = new HashMap<String, Object>();
       
       if (search.getCurrentPage() == 0) {
            search.setCurrentPage(1);
         }
             
      search.setPageSize(10);
      search.setId(user.getId()); // listMyTransfer??? search??? id??? ?????????. 

      map.put("search", search);
      
      System.out.println(" -------------- \n options ==>"+ options);
      
      if(options == 1) {
         map = transferService.listTransfer(map);
      }else {
         map = transferService.listTransferForReceive(search);
         
      }
      
      TotalCount = (int) map.get("TotalCount");
      Transferlist =  (List<Transfer>) map.get("list");
   
      
      Page resultPage = new Page( search.getCurrentPage(), TotalCount, pageUnit, pageSize);
            
      System.out.println(Transferlist);
      
      model.addAttribute("user", user);
      model.addAttribute("Transferlist", Transferlist);
      model.addAttribute("resultPage", resultPage);
      model.addAttribute("options", options);
      
      return "forward:/view/transfer/listMyTransfer.jsp";
   }
   
   
   
   // ???????????? My ????????? navigation   
   
//   @RequestMapping(value = "listMyReceive")   
//   
//   public String listMyReceive(HttpSession session, Model model) throws Exception {
//
//      System.out.println("listMyTransfer Start");
//
//      User user = (User) session.getAttribute("user");
//
//      if (user == null) {
//         return "redirect:/";
//      }
//         
//       Map<String, Object> map = new HashMap<String, Object>();
//       
//       Search search = new Search();
//           
//       if (search.getCurrentPage() == 0) {
//            search.setCurrentPage(1);
//         }
//             
//      search.setPageSize(10);
//      search.setId(user.getId()); // listMyTransfer??? search??? id??? ?????????. 
//
//      map.put("search", search);
//             
//      map = transferService.listTransfer(map);
//      
//      int TotalCount = (int) map.get("TotalCount");
//   
//      List<Transfer> Transferlist =  (List<Transfer>) map.get("list");
//      
//      Page resultPage = new Page( search.getCurrentPage(), TotalCount, pageUnit, pageSize);
//            
//      System.out.println(Transferlist);
//      
//      model.addAttribute("user", user);
//      model.addAttribute("Transferlist", Transferlist);
//      model.addAttribute("resultPage", resultPage);      
//      
//      return "forward:/view/transfer/listMyTransfer.jsp";
//   }
   
   
  // ???????????? ?????? 
   @RequestMapping(value = "TransferPayment")   
   public String TransferPayment(@RequestParam(value = "receiveno", required = false) String receiveno, @RequestParam("transferor") String transferor, @RequestParam("transferPrice") String transferPrice, @RequestParam("transferno") String transferno, Model model, HttpSession session, Search search) throws Exception {

     System.out.println("listMyTransfer Start");

     User user = (User) session.getAttribute("user");

      if (user == null) {
        return "redirect:/";
     }
      
      System.out.println("transferor::"+transferor);
      System.out.println("transferPrice::"+transferPrice);
      System.out.println("transferno::"+transferno);
      System.out.println("receiveno::"+receiveno);
      System.out.println("trasnferee::"+user.getId());
      
      Payment payment = new Payment();
      
      payment.setPaymentProduct("????????????");    
      payment.setPaymentSender(user.getId()); //????????? ?????? ID    
      payment.setPaymentReceiver(transferor);  //?????? ?????? ID    
      payment.setPaymentCode("T1");     
      payment.setPaymentReferenceNum(transferno);     
      payment.setPaymentMethodSecond("point");    
      payment.setPaymentPriceTotalSecond(Integer.parseInt(transferPrice));

      paymentService.makePayment(payment);
      
      User tempUser = userService.getUser( ((User) session.getAttribute("user")).getId() );
      session.removeAttribute("user");
      session.setAttribute("user", tempUser);
      
      
      //????????? ??????
      
      //?????? ????????? ?????? ???????????? ?????????????????? ??????????????? ????????? 3?????? ???????????? ???????????? ????????? 4??? ????????????. 
      
      
      Receive receive = new Receive();
      Transfer transfer = new Transfer();
      
      transfer.setTransferNo(Integer.parseInt(transferno));     
      receive.setReceiveStatus(2);
      receive.setTransferNo(transfer);
      receive.setReceiveNo(Integer.parseInt(receiveno));
      
      receiveService.updateTransferStatus(receive);
           
     return "redirect:/view/transfer/listMyTransfer.jsp";
     
     
    
   }
  
  
   
 //??????????????? add
//   
//   @RequestMapping(value = "addReceive") 
//   public String addReceive (@ModelAttribute("receive") Receive receive, String transferNoo, HttpSession session, Model model) throws Exception{
//  	 
//  	 System.out.println("addReceive:::");
//  	 System.out.println(receive);
//
//  	 Transfer transfer = new Transfer();
//
//  	 transfer.setTransferNo(Integer.parseInt(transferNoo));
//  	 receive.setTransferNo(transfer);
//  	 
//  	 User user = (User)session.getAttribute("user");
//  	 receive.setTransferee(user);
//  	 
//  	 System.out.println("receive getTransfer => "+ receive.getTransferNo());
////  	 System.out.println("receive => " + receive);
//  	 receiveService.addReceive(receive);
//  	 
//  	 System.out.println(transferNoo);
//  	 
//     model.addAttribute("transferNo", transferNoo);      
//  		
//  		return "forward:getTransfer";
//   }
   
   
   //????????????page ???????????????   
   
   @RequestMapping(value = "updateTransfer", method= RequestMethod.GET) 
   public String updateTransfer (@RequestParam(value = "transferNo", required = false)int transferNo, HttpSession session, Model model) throws Exception{
  	 
  	 System.out.println("updateTransfer:::");
  	 System.out.println(transferNo);
	 
  	 User user = (User)session.getAttribute("user");
  	 
     if (user == null) {
         return "redirect:/";
      }
     
     Search search = new Search();
     String id = user.getId();      
     
     if (search.getCurrentPage() == 0) {
          search.setCurrentPage(1);
       }
       
     search.setPageSize(10);
    
  	 Map<String, Object> List   = campReservationService.listMyReservation(search, id);
  	 
     List<CampReservation> list = (List<CampReservation>) List.get("list");
     
  	 
  	Transfer transfer = transferService.getTransfer(transferNo);
  	 
  	
  	System.out.println("\n\n Transfer ==>" + transfer );
  	
  	
     model.addAttribute("list", list);
     model.addAttribute("transfer", transfer);

  		
     return "forward:/view/transfer/updateTransfer.jsp";
   }
   
   
   
   
   // ?????????????????? ????????????
   
   @RequestMapping(value = "updateTransfer", method = RequestMethod.POST) // RequestParam??? ????????? file type????????? name??? ?????????.
   public String updateTransfer(@ModelAttribute("transfer") Transfer transfer, HttpServletRequest req, HttpSession session, Model model) throws Exception {

      System.out.println("updateTransfer Post Start");
      System.out.println("transfer:::::::"+transfer);

      	 User user = (User)session.getAttribute("user");
      	 
         if (user == null) {
             return "redirect:/";
          }
                          
        transfer.setTransferOr(user);

        System.out.println("????????????????????? ?????? ???????????? + ????????? ?????? user??? transferor??? ?????????????"+transfer);
        
    	transferService.updateTransfer(transfer);
    	
//      ???????????? ??? ??????????????????. 
         
      return "redirect:listTransfer";
   }// ?????? method ?????? 
   
   
   
   
   
   //????????????page ???????????????   
   
   @RequestMapping(value = "deleteTransfer") 
   public String deleteTransfer (@RequestParam(value = "transferNo", required = false)int transferNo, HttpSession session, Model model) throws Exception{
  	 
  	 System.out.println("deleteTransfer:::");
  	 System.out.println(transferNo);
	 
  	 User user = (User)session.getAttribute("user");
  	 
     if (user == null) {
         return "redirect:/";
      }
     
     transferService.deleteTransfer(transferNo);
  	   	   		
     return "redirect:listTransfer";
   }   
   
   
   
   
   
   
   
   
   
   
   
//   //@Test 
//	 public void testUpdateTransfer() throws Exception {		
//	 
//	 Transfer transfer = transferService.getTransfer(1);
//	 
//	 transfer.setTransferTitle("??????????????? ????????? ????????? ?????????"); //???????????? 1
//	 transfer.setTransferCampname("?????????");  //???????????? 2 
//	 transfer.setTransferMainsiteType("??????");  //????????????????????? 3 
//	 transfer.setTransferStartDate("2021-12-31");  //?????????????????? 4 
//	 transfer.setTransferEndDate("2022-01-15");  //??????????????? 5 
//	 transfer.setTransferCampCall("03112345678");  //??????????????? 6 
//	 transfer.setTransferUserNum(5);  //???????????? 7
//	 transfer.setTransferPrice(10000);  //???????????? 8 
//	 transfer.setTransferContent("????????? ?????? ??? ????????? ??????????????? ???????????? ?????????.");  //???????????? 9
//	 transfer.setTransferAddContent("????????? ???????????? ???????????????????????? ");  //??????????????????  10 
//	 transfer.setPaymentImg("AABBCCDDEEFFGGHHIIJJKKLL");  //??????????????? 11
//	 transfer.setHashtag1("#?????????");  //????????????1 12
//	 transfer.setHashtag2("#?????????");  //????????????2 13 
//	 transfer.setHashtag3("#?????????");  //????????????3 14 
//	
//	 int TRANSFER = transferService.updateTransfer(transfer);
//	
//	System.out.println("TRANSFER:::::::::::::::::::::::::::::::::" + TRANSFER);		 
//	}
//   
 
   
   
   
   
   
   
   
   
   
   
   
     
   
}