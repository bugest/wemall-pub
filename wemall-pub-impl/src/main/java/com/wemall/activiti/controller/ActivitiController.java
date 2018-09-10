/**************************************************
 *
 *    AMNET Copyright (c) 2004-2017
 *
 *    Package:     org.am.modules.hr.controller.activiti
 *    Filename:    ActivitiController.java
 *
 *    @author:     ln
 *    @since:      2016年6月29日
 *    @version:    
 *
 **************************************************/
package com.wemall.activiti.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.interceptor.Command;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ModelAndView;

import com.wemal.common.model.PageData;
import com.wemall.activiti.service.impl.ActivitiService;
import com.wemall.common.utils.ProcessDefinitionDiagramCmd;
import com.wemall.transfer.model.HRWorkFlowBean;



/**
 * activiti工作流的controller
 *
 * @ClassName ActivitiController
 * @author ln
 * @since 2016年6月29日
 * @version
 */
@Controller
@RequestMapping("activiti")
public class ActivitiController {
    @Autowired
    private ActivitiService activitiService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private ManagementService managementService;
    @Autowired
    private TaskService traceService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private CommonsMultipartResolver multipartResolver;

    // @Autowired
    // private HumanTaskConnector humanTaskConnector;

    /**
     * 通过人员找代办任务
     *
     * @author ln
     * @since 2016年6月29日
     * @version
     * @param name
     * @return
     * @return: List<Task>
     */
    @RequiresRoles(value = { "developers" })
    @ResponseBody
    @RequestMapping("findtasklistbylogid")
    public PageData<Map<String, Object>> findTaskListByName(int pagenum, int pagesize, String logid, Date beginDate,
            Date endDate) {
        PageData<Map<String, Object>> pageDate = activitiService.findTaskListByName(pagenum, pagesize, logid, beginDate,
                endDate);
        List<Map<String, Object>> list = pageDate.getContent();
        /*for (Map<String, Object> m : list) {
            m.put("handleHref",
                    "<a href='forward?taskid=" + (String) m.get("id") + "'>"
                            + MessageUtils.message("biz.activiti.herf.handle") + "</a>&nbsp;" + "<a href='getprocessdetailsbytaskid?taskId="
                            + (String) m.get("id") + "'>" + MessageUtils.message("biz.activiti.herf.detail") + "</a>");
        }*/
        return pageDate;
    }

    /**
     * 获取代办任务列表
     *
     * @author ln
     * @since 2016年6月29日
     * @version
     * @return
     * @return: String
     */
    @RequiresRoles(value = { "developers" })
    @RequestMapping("gettasklist")
    public String getTaskList() {
        return "activiti/task/todotasklist.jsp";
    }

    /**
     * 将不同种类的任务分发到不同的视图上
     *
     * @author ln
     * @since 2016年6月30日
     * @version
     * @param taskid
     * @return
     * @return: ModelAndView
     */
    @RequiresRoles(value = { "developers" })
    @RequestMapping("forward")
    public ModelAndView forwardTaskForm(String taskid) {
        return activitiService.forwardTaskForm(taskid);

    }

    @RequiresRoles(value = { "developers" })
    @RequestMapping("getlogid")
    public String gegLogid() {
        return "胡大龙";
    }

    @RequiresRoles(value = { "developers" })
    @RequestMapping("flowback")
    public String flowBack(String taskid, String destTaskKey) {
        // 通过taskid 找到流程实例
        String procInstId = activitiService.findProcInstIdByTaskId(taskid);
        activitiService.rejectTask(procInstId, destTaskKey);
        return "redirect:../activiti/gettasklist";
    }

    @RequiresRoles(value = { "developers" })
    @ResponseBody
    @RequestMapping("findcompletedtasklistbylogid")
    public PageData<Map<String, Object>> findAllTaskListByName(int pagenum, int pagesize, String logid, String state,
            Date beginDate, Date endDate) {
        return activitiService.findCompletedTaskListByName(pagenum, pagesize, logid, state, beginDate, endDate);
    }

    @RequiresRoles(value = { "developers" })
    // @ResponseBody
    @RequestMapping("findcompletedtask")
    public String findcompletedtask() {
        return "activiti/task/completedtasklist.jsp";
    }


    /**
     * TODO
     *
     * @author ln
     * @since 2016年9月7日
     * @version
     * @param workflowBean
     * @param response
     * @return: void
     */
    @RequiresRoles(value = { "developers" })
    @RequestMapping("viewprocessImage")
    public void viewprocessImage(HRWorkFlowBean workflowBean, HttpServletResponse response) {
        String taskId = workflowBean.getTaskId();
        String executionId = taskService.createTaskQuery().taskId(taskId).singleResult().getExecutionId();
        try {
            activitiService.readResource(executionId, response);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @RequestMapping("viewdetail")
    public String viewDetail(HRWorkFlowBean workflowBean, HttpServletResponse response) {
        return "activiti/bpm/viewdetail.jsp";
    }
    
    @RequestMapping("savenewdeploy")
    public String saveNewDeploy(HttpServletRequest request, HttpServletResponse response)
            throws IllegalStateException, IOException {
        //CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
          //      );
        //multipartResolver.setMaxUploadSize(0);
        multipartResolver.setServletContext(request.getSession().getServletContext());
        if (multipartResolver.isMultipart(request)) {
            //定义文件上传的路径
            String dirPath = request.getSession().getServletContext().getRealPath("/") + "flowupload/";
            //根据地址创建文件实例
            File dir = new File(dirPath); 
            //如果不存在这个路径，就建立一个
            if(!dir.exists()){
                dir.mkdir();
            }
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                MultipartFile file = multiRequest.getFile((String)iter.next());
                if (file != null&&file.getSize()>0) {
                    String filename = file.getOriginalFilename();//request.getParameter("filename");
                            //file.getOriginalFilename();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
                    String dateStr = format.format(new Date());
                    //将时间与文件名联合
                    String filePath = dirPath + dateStr + '-' + filename;
                    // 转存文件
                    File myfile = new File(filePath);
                    file.transferTo(myfile);
                    activitiService.saveNewDeploy(myfile, dateStr + '-' + filename);
                }
            }
        }
        return "redirect:viewprocessdef";
    }

    /**
     * 上传流程
     *
     * @author ln
     * @since 2016年9月9日
     * @version
     * @return
     * @return: String
     */
    @RequestMapping("uploadflow")
    public String uploadFlow() {
        return "activiti/modeler/deploy/deploy.jsp";
    }
    
    /**
     * 通过一个任务的id查到这个流程所有的过程
     *
     * @author ln
     * @since 2016年9月12日
     * @version 
     * @param pagenum
     * @param pagesize
     * @param taskid
     * @return
     * @return: PageData<Map<String,Object>>
     */
    @ResponseBody
    @RequestMapping("findhistorytasksbytaskid")
    public PageData<Map<String, Object>> findHistoryTasksBytaskid(int pagenum, int pagesize, String taskid){
        PageData<Map<String, Object>> pageDate = activitiService.findHistoryTasksBytaskid(pagenum, pagesize, taskid);    
        return pageDate;
    }
    
    /**
     * 通过任务id获取整个流程的详情
     *
     * @author ln
     * @since 2016年9月12日
     * @version 
     * @param taskId
     * @return
     * @return: String
     */
    @RequestMapping("getprocessdetailsbytaskid")
    public ModelAndView getProcessDetailsByTaskId(String taskId){
        Map<String, String> map = new HashMap<String, String>();
        map.put("taskId", taskId);
        return new ModelAndView("activiti/bpm/viewdetail.jsp", map);
    }
    
    
    /**
     * 得到流程定义的分页数据
     *
     * @author ln
     * @since 2016年9月14日
     * @version 
     * @param pagenum
     * @param pagesize
     * @param key
     * @param name
     * @return
     * @return: PageData<ProcessDefinition>
     */
    @ResponseBody
    @RequestMapping("getprocessdefpagedata")
    public PageData<Map<String,Object>> getProcessDefPageData(int pagenum, int pagesize, String key, String name){
        PageData<Map<String,Object>> pageDate = activitiService.getProcessDefPage(pagenum, pagesize, key, name);        
        return pageDate;
    }
    
    /**
     * 流程定义页
     *
     * @author ln
     * @since 2016年9月14日
     * @version 
     * @param pagenum
     * @param pagesize
     * @return
     * @return: String
     */
    @RequestMapping("viewprocessdef")
    public String viewProcessDef(){
        return "activiti/process/prodef.jsp";            
    }
    
    /**
     * 查看流程图
     *
     * @author ln
     * @since 2016年9月18日
     * @version 
     * @param processDefinitionId
     * @param response
     * @throws Exception
     * @return: void
     */
    @RequestMapping("graphprocessdefinition")
    public void graphProcessDefinition(
            @RequestParam("processDefinitionId") String processDefinitionId,
            HttpServletResponse response) throws Exception {
        Command<InputStream> cmd = new ProcessDefinitionDiagramCmd(
                processDefinitionId);
        InputStream is = managementService.executeCommand(
                cmd);
        response.setContentType("image/png");
        IOUtils.copy(is, response.getOutputStream());
    }
    
}
