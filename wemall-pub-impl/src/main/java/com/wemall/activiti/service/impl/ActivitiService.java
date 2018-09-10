/**************************************************
 *
 *    AMNET Copyright (c) 2004-2017
 *
 *    Package:     org.am.modules.common.service
 *    Filename:    ActivitiService.java
 *
 *    @author:     ln
 *    @since:      2016年6月30日
 *    @version:    
 *
 **************************************************/
package com.wemall.activiti.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import com.wemal.common.model.PageData;
import com.wemall.activiti.model.TaskDetailsModel;
import com.wemall.activiti.model.WorkFlowBean;

/**
 * TODO
 *
 * @ClassName ActivitiService
 * @author ln
 * @since 2016年6月30日
 * @version
 */
@Service
public class ActivitiService {
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private FormService formService;
    @Autowired
    private HistoryService historyService;

    /**
     * TODO
     *
     * @author ln
     * @since 2016年6月30日
     * @version
     * @param pagenum
     * @param pagesize
     * @param logid
     * @param endDate 
     * @param beginDate 
     * @return
     * @return: List<Map<String,String>>
     */
    public PageData<Map<String, Object>> findTaskListByName(int pagenum, int pagesize, String logid, Date beginDate, Date endDate) {
        // TODO Auto-generated method stub
        PageData<Map<String, Object>> taskPageData = new PageData<Map<String, Object>>();
        List<Task> list = taskService.createTaskQuery()//
                .taskAssignee(logid)
                .taskCreatedBefore(endDate)
                .taskCreatedAfter(beginDate)// 指定个人任务查询
                .orderByTaskCreateTime().desc()//
                .listPage(pagenum * pagesize, pagesize);
        //SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        taskPageData.setTotalElements(taskService.createTaskQuery()//
                .taskAssignee(logid)
                .taskCreatedBefore(endDate)
                .taskCreatedAfter(beginDate)
                .count());
        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
        for (Task t : list) {
            Map<String, Object> m = new HashMap<String, Object>();
            String startUserId = historyService.createHistoricProcessInstanceQuery().processInstanceId(t.getProcessInstanceId()).singleResult().getStartUserId();
            //通过任务id找到
            //System.out.println(runtimeService.createProcessInstanceQuery().processInstanceId(t.getProcessInstanceId()).singleResult().getBusinessKey());
            //  
            TaskDetailsModel taskDetailsModel =(TaskDetailsModel)taskService.getVariable(t.getId(), "taskdetails");
            String subject = null;
            if(taskDetailsModel!=null){
                subject = t.getName()+ " :" + taskDetailsModel.getOrderNo() + "：" + startUserId;
                m.put("submitter", taskDetailsModel.getSubmitter());
            }
            else
                subject = t.getName() + "：" + startUserId;  
            m.put("flowName", repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionId(runtimeService
                    .createProcessInstanceQuery()
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult()
                    .getProcessDefinitionId())
                    .singleResult()
                    .getName());
            //t.getProcessDefinitionId()
            m.put("subject", subject);
            m.put("id", t.getId());
            m.put("name", t.getName());
            m.put("assignee", t.getAssignee());
            //m.put("createTime", formatDate.format(t.getCreateTime()));
            m.put("createTime", t.getCreateTime());
            m.put("handleHref", "<a href='forward?taskid=" + t.getId() + "'>办理</a>&nbsp;" + "<a href='" + t.getId() + "'>详情</a>");
            returnList.add(m);
        }
        taskPageData.setContent(returnList);
        return taskPageData;
    }

    /**
     * 通过taskid跳转到指定的任务界面并且传递业务主键
     *
     * @author ln
     * @since 2016年6月30日
     * @version
     * @param taskid
     * @return
     * @return: ModelAndView
     */
    public ModelAndView forwardTaskForm(String taskid) {
        // 获取当前任务的formkey
        String formKey = formService.getTaskFormData(taskid).getFormKey();
        Map<String, Object> model = new HashMap<String, Object>();
        String id = findIdByTaskId(taskid);
        model.put("id", id);
        model.put("taskid", taskid);
        // return null;
        return new ModelAndView("forward:../" + formKey, model);
    }

    /**
     * 通过taskid找到关联的业务主键
     *
     * @author ln
     * @since 2016年6月30日
     * @version
     * @return
     * @return: String
     */
    public String findIdByTaskId(String taskid) {
        // 1：使用任务ID，查询任务对象Task
        Task task = taskService.createTaskQuery()//
                .taskId(taskid)// 使用任务ID查询
                .singleResult();
        // 2：使用任务对象Task获取流程实例ID
        String processInstanceId = task.getProcessInstanceId();
        // 3：使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
                .processInstanceId(processInstanceId)// 使用流程实例ID查询
                .singleResult();
        // 4：使用流程实例对象获取BUSINESS_KEY
        String buniness_key = pi.getBusinessKey();
        // 5：获取BUSINESS_KEY对应的主键ID，使用主键ID，查询请假单对象（LeaveBill.1）
        String id = "";
        if (StringUtils.isNotBlank(buniness_key)) {
            // 截取字符串，取buniness_key小数点的第2个值
            id = buniness_key.split("\\.")[1];
        }
        return id;
    }

    /**
     * 发起流程，同过key发起
     *
     * @author ln
     * @since 2016年7月1日
     * @version
     * @param key
     *            流程的key
     * @param id
     *            主键值
     * @param logid
     *            登录者的id
     * @return: void
     */
    @Transactional
    public void saveStartProcess(String key, String id, String logid) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("logid", logid);// 表示惟一用户
        // 格式：LeaveBill.id的形式（使用流程变量）
        String objId = key + "." + id;
        variables.put("objId", objId);
        // 6：使用流程定义的key，启动流程实例，同时设置流程变量，同时向正在执行的执行对象表中的字段BUSINESS_KEY添加业务数据，同时让流程关联业务
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, objId, variables);
    }

    /**
     * 将任务流转出去
     *
     * @author ln
     * @since 2016年7月2日
     * @version
     * @param workflowBean
     * @param logid
     * @return: void
     */
    @Transactional
    public void flowoutTask(WorkFlowBean workflowBean, String logid, String state) {
        // 获取任务ID
        String taskId = workflowBean.getTaskId();
        String assignee = workflowBean.getAssignee();
        // 获取请假单ID
        String id = workflowBean.getId();
        // 使用任务ID，查询任务对象，获取流程流程实例ID
        Task task = taskService.createTaskQuery()//
                .taskId(taskId)// 使用任务ID查询
                .singleResult();
        // 获取流程实例ID
        String processInstanceId = task.getProcessInstanceId();
        /**
         * 注意：添加批注的时候，由于Activiti底层代码是使用： String userId =
         * Authentication.getAuthenticatedUserId(); CommentEntity comment = new
         * CommentEntity(); comment.setUserId(userId);
         * 所有需要从Session中获取当前登录人，作为该任务的办理人（审核人），对应act_hi_comment表中的User_ID的字段，
         * 不过不添加审核人，该字段为null
         * 所以要求，添加配置执行使用Authentication.setAuthenticatedUserId();添加当前任务的审核人
         */
        Authentication.setAuthenticatedUserId(logid);
        /**
         * 2：如果连线的名称是“默认提交”，那么就不需要设置，如果不是，就需要设置流程变量 在完成任务之前，设置流程变量，按照连线的名称，去完成任务
         * 流程变量的名称：outcome 流程变量的值：连线的名称
         */
        Map<String, Object> variables = new HashMap<String, Object>();

        // 3：使用任务ID，完成当前人的个人任务，同时流程变量
        taskService.complete(taskId, variables);
        // 4：当任务完成之后，需要指定下一个任务的办理人（使用类）-----已经开发完成

        /**
         * 5：在完成任务之后，判断流程是否结束 如果流程结束了，更新请假单表的状态从1变成2（审核中-->审核完成）
         */
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
                .processInstanceId(processInstanceId)// 使用流程实例ID查询
                .singleResult();
        /*
         * if(pi==null){ //更新请假单表的状态从1变成2（审核中-->审核完成） HRDimission bill =
         * leaveBillDaoInterface.findLeaveBillById(id); bill.setState(2); }
         */

    }
    
    /**
     * 通过taskid找到实例id
     *
     * @author ln
     * @since 2016年7月8日
     * @version 
     * @param taskId
     * @return
     * @return: String
     */
    public String findProcInstIdByTaskId(String taskId)
    {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null)
            return task.getProcessInstanceId();
        else
            return "";
    }
    /**
     * 隐形回退
     *
     * @author ln
     * @since 2016年7月8日
     * @version
     * @param procInstId
     * @param destTaskKey
     * @param rejectMessage
     * @return: void
     */
    public void rejectTask(String procInstId, String destTaskKey) {
        // WorkflowResult result =
        // WorkflowResult.createInstance(WorkflowReturnCode.SUCCESS,
        // WorkflowContants.SUCCESS_MESSAGE);
        try {
            // 获得当前任务的对应实列
            Task taskEntity = taskService.createTaskQuery().processInstanceId(procInstId).singleResult();
            // 当前任务key
            String taskDefKey = taskEntity.getTaskDefinitionKey();
            // 获得当前流程的定义模型
            ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                    .getDeployedProcessDefinition(taskEntity.getProcessDefinitionId());

            // 获得当前流程定义模型的所有任务节点
            List<ActivityImpl> activitilist = processDefinition.getActivities();
            // 获得当前活动节点和驳回的目标节点"draft"
            ActivityImpl currActiviti = null;// 当前活动节点
            ActivityImpl destActiviti = null;// 驳回目标节点
            int sign = 0;
            for (ActivityImpl activityImpl : activitilist) {
                // 确定当前活动activiti节点
                if (taskDefKey.equals(activityImpl.getId())) {
                    currActiviti = activityImpl;
                    sign++;
                } else if (destTaskKey.equals(activityImpl.getId())) {
                    destActiviti = activityImpl;
                    sign++;
                }
                // System.out.println("//-->activityImpl.getId():"+activityImpl.getId());
                if (sign == 2) {
                    break;// 如果两个节点都获得,退出跳出循环
                }
            }
            System.out.println("//-->currActiviti activityImpl.getId():" + currActiviti.getId());
            System.out.println("//-->destActiviti activityImpl.getId():" + destActiviti.getId());
            // 保存当前活动节点的流程想参数
            List<PvmTransition> hisPvmTransitionList = new ArrayList<PvmTransition>(0);

            for (PvmTransition pvmTransition : currActiviti.getOutgoingTransitions()) {
                hisPvmTransitionList.add(pvmTransition);
            }
            // 清空当前活动几点的所有流出项
            currActiviti.getOutgoingTransitions().clear();
            System.out.println("//-->currActiviti.getOutgoingTransitions().clear():"
                    + currActiviti.getOutgoingTransitions().size());
            // 为当前节点动态创建新的流出项

            TransitionImpl newTransitionImpl = currActiviti.createOutgoingTransition();
            // 为当前活动节点新的流出目标指定流程目标
            newTransitionImpl.setDestination(destActiviti);
            // 保存驳回意见

            //taskEntity.setDescription(rejectMessage);// 设置驳回意见
            taskService.saveTask(taskEntity);
            // 设定驳回标志

            Map<String, Object> variables = new HashMap<String, Object>(0);
            // variables.put(WfConstant.WF_VAR_IS_REJECTED.value(),
            // WfConstant.IS_REJECTED.value());
            // 执行当前任务驳回到目标任务draft
            taskService.complete(taskEntity.getId(), variables);
            // 清除目标节点的新流入项
            destActiviti.getIncomingTransitions().remove(newTransitionImpl);
            // 清除原活动节点的临时流程项
            currActiviti.getOutgoingTransitions().clear();
            // 还原原活动节点流出项参数
            currActiviti.getOutgoingTransitions().addAll(hisPvmTransitionList);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * TODO
     *
     * @author ln
     * @since 2016年8月19日
     * @version 
     * @param pagenum
     * @param pagesize
     * @param logid
     * @param state
     * @param endDate 
     * @param beginDate 
     * @return
     * @return: List<Map<String,Object>>
     */
    public PageData<Map<String, Object>> findCompletedTaskListByName(int pagenum, int pagesize, String logid, String state, Date beginDate, Date endDate) {
        // TODO Auto-generated method stub
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()//
                .taskAssignee(logid)
                .taskDeleteReason("completed")
                .taskCreatedBefore(endDate)
                .taskCreatedAfter(beginDate)
                .orderByTaskCreateTime().desc()//
                .listPage(pagenum * pagesize, pagesize);
        PageData<Map<String, Object>> taskPageData = new PageData<Map<String, Object>>();
        taskPageData.setTotalElements(historyService.createHistoricTaskInstanceQuery()//
                .taskAssignee(logid)
                .taskDeleteReason("completed")
                .taskCreatedBefore(endDate)
                .taskCreatedAfter(beginDate)
                .count());
        //SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
        for (HistoricTaskInstance t : list) {
            String startUserId = historyService.createHistoricProcessInstanceQuery().processInstanceId(t.getProcessInstanceId()).singleResult().getStartUserId();
            Map<String, Object> m = new HashMap<String, Object>();
            //通过任务id找到
            //System.out.println(runtimeService.createProcessInstanceQuery().processInstanceId(t.getProcessInstanceId()).singleResult().getBusinessKey());
            //  
            List<HistoricVariableInstance> hviList = historyService.createHistoricVariableInstanceQuery().processInstanceId(t.getProcessInstanceId()).list();
            String subject = null; 
            for (HistoricVariableInstance historicVariableInstance :hviList){
                if (historicVariableInstance.getVariableName().equals("taskdetails")){
                    TaskDetailsModel taskDetailsModel =(TaskDetailsModel)historicVariableInstance.getValue();
                    if(taskDetailsModel!=null){
                        subject = t.getName()+ " :" + taskDetailsModel.getOrderNo() + "：" + startUserId;
                        //m.put("submitter", taskDetailsModel.getSubmitter());
                    }       
                }
            }
            if(subject==null||subject.equals("")){
                subject = t.getName() + "：" + startUserId;  
            } 
            m.put("flowName", repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionId(historyService
                    .createHistoricProcessInstanceQuery()
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult()
                    .getProcessDefinitionId()).singleResult().getName());
            m.put("subject", subject);
            m.put("id", t.getId());
            m.put("endTime", t.getEndTime());
            m.put("duration", t.getDurationInMillis());
            m.put("name", t.getName());
            m.put("assignee", t.getAssignee());
            //m.put("createTime", formatDate.format(t.getCreateTime()));
            m.put("createTime", t.getStartTime());
            //m.put("handleHref", "<a href='forward?taskid=" + t.getId() + "'>办理</a>");
            returnList.add(m);
        }
        taskPageData.setContent(returnList);
        return taskPageData;
    }
    public void readResource(String executionId,
            HttpServletResponse response) throws Exception {
        List<HistoricActivityInstance> activityInstances = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(executionId)
                .orderByHistoricActivityInstanceStartTime().asc().list();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(activityInstances
                .get(0).getProcessDefinitionId());
        List<String> activitiIds = new ArrayList<String>();
        List<String> flowIds = new ArrayList<String>();
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(activityInstances.get(0)
                        .getProcessDefinitionId());
        flowIds = getHighLightedFlows(processDefinition, activityInstances);// 获取流程走过的线
        for (HistoricActivityInstance hai : activityInstances) {
            activitiIds.add(hai.getActivityId());// 获 取流程走过的节点
        }
        ProcessEngineImpl defaultProcessEngine = (ProcessEngineImpl) ProcessEngines
                .getDefaultProcessEngine();
        Context.setProcessEngineConfiguration(defaultProcessEngine
                .getProcessEngineConfiguration());
        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngine
                .getProcessEngineConfiguration());
        InputStream imageStream = processEngine.getProcessEngineConfiguration()
                .getProcessDiagramGenerator()
                .generateDiagram(bpmnModel, "png", activitiIds, flowIds,processEngine.getProcessEngineConfiguration().getActivityFontName(), 
                        processEngine.getProcessEngineConfiguration().getLabelFontName(), null, 1.0);
        
        OutputStream os = response.getOutputStream();
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        while ((bytesRead = imageStream.read(buffer, 0, 8192)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
        imageStream.close();
    }

    /**
     * 划线
     *
     * @author ln
     * @since 2016年9月7日
     * @version 
     * @param processDefinitionEntity
     * @param historicActivityInstances
     * @return
     * @return: List<String>
     */
    public List<String> getHighLightedFlows(
            ProcessDefinitionEntity processDefinitionEntity,
            List<HistoricActivityInstance> historicActivityInstances) {
        List<String> highFlows = new ArrayList<String>();// 用 以保存高亮的线flowId
        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {
            // 对历史流程节点进行遍历
            ActivityImpl activityImpl = processDefinitionEntity
                    .findActivity(historicActivityInstances.get(i)
                            .getActivityId());
            // 得到节点定义的详细 信息
            List<ActivityImpl> sameStartTimeNodes = new ArrayList<ActivityImpl>();
            // 用以保存后需开始时间相同的节点
            ActivityImpl sameActivityImpl1 = processDefinitionEntity
                    .findActivity(historicActivityInstances.get(i + 1)
                            .getActivityId());
            // 将后面第一个节点放 在时间相同节点的集合里
            sameStartTimeNodes.add(sameActivityImpl1);
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImpl1 = historicActivityInstances
                        .get(j);
                // 后续第一个节点
                HistoricActivityInstance activityImpl2 = historicActivityInstances
                        .get(j + 1);
                // 后续第二个节点
                if (activityImpl1.getStartTime().equals(
                        activityImpl2.getStartTime())) {
                    // 如果第 一个节点和第二个节点开始时间相同保存
                    ActivityImpl sameActivityImpl2 = processDefinitionEntity
                            .findActivity(activityImpl2.getActivityId());
                    sameStartTimeNodes.add(sameActivityImpl2);
                } else {
                    // 有不相同跳出循环
                    break;
                }
            }
            List<PvmTransition> pvmTransitions = activityImpl
                    .getOutgoingTransitions();
            // 取出节点的所有出 去的线
            for (PvmTransition pvmTransition : pvmTransitions) {
                // 对所有的线进行遍历
                ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition
                        .getDestination();
                // 如果取出的线的目标节点 存在时间相同的节点里，保存该线的id，进行高亮显示
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
                    highFlows.add(pvmTransition.getId());
                }
            }
        }
        
        return highFlows;
    }   
    
    /**
     * 保存发布的流程
     *
     * @author ln
     * @since 2016年9月8日
     * @version 
     * @param file
     * @param filename
     * @return: void
     */
    public void saveNewDeploy(File file, String filename) {
        // TODO Auto-generated method stub
        try {
            //2：将File类型的文件转化成ZipInputStream流
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
            repositoryService.createDeployment()//创建部署对象
                            .name(filename) //添加部署名称
                            .addZipInputStream(zipInputStream)//
                            .deploy();//完成部署
        } 
        catch (Exception e) {
            e.printStackTrace();
        }       
    }

    /**
     * 通过当前的taskid 找到属于这个流程的所有的历史task
     *
     * @author ln
     * @since 2016年9月10日
     * @version 
     * @param pagenum
     * @param pagesize
     * @param taskid
     * @return
     * @return: PageData<Map<String,Object>>
     */
    public PageData<Map<String, Object>> findHistoryTasksBytaskid(int pagenum, int pagesize, String taskid) {
        PageData<Map<String, Object>> taskPageData = new PageData<Map<String, Object>>();
        //通过id定位到流程实例
        String instanceId = historyService.createHistoricTaskInstanceQuery().taskId(taskid).singleResult().getProcessInstanceId();
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processInstanceId(instanceId)//
                .orderByTaskCreateTime().desc()//
                .listPage(pagenum * pagesize, pagesize);
        //SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        taskPageData.setTotalElements(historyService
                .createHistoricTaskInstanceQuery()
                .processInstanceId(instanceId)
                .count());
        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
        for (HistoricTaskInstance t : list) {
            Map<String, Object> m = new HashMap<String, Object>();
            String startUserId = historyService.createHistoricProcessInstanceQuery().processInstanceId(t.getProcessInstanceId()).singleResult().getStartUserId();
            //通过任务id找到
            List<HistoricVariableInstance> hviList = historyService.createHistoricVariableInstanceQuery().processInstanceId(t.getProcessInstanceId()).list();
            String subject = null; 
            for (HistoricVariableInstance historicVariableInstance :hviList){
                if (historicVariableInstance.getVariableName().equals("taskdetails")){
                    TaskDetailsModel taskDetailsModel =(TaskDetailsModel)historicVariableInstance.getValue();
                    if(taskDetailsModel!=null){
                        subject = t.getName()+ " :" + taskDetailsModel.getOrderNo() + "：" + startUserId;
                        //m.put("submitter", taskDetailsModel.getSubmitter());
                    }       
                }
            }
            if(subject==null||subject.equals("")){
                subject = t.getName() + "：" + startUserId;  
            } 
            m.put("flowName", repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionId(runtimeService
                    .createProcessInstanceQuery()
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult()
                    .getProcessDefinitionId())
                    .singleResult()
                    .getName());
            //t.getProcessDefinitionId()
            m.put("subject", subject);
            m.put("id", t.getId());
            m.put("name", t.getName());
            m.put("assignee", t.getAssignee());
            //m.put("createTime", formatDate.format(t.getCreateTime()));
            m.put("createTime", t.getCreateTime());
            m.put("endTime", t.getEndTime());
            //m.put("handleHref", "<a href='forward?taskid=" + t.getId() + "'>办理</a>&nbsp;" + "<a href='" + t.getId() + "'>详情</a>");
            returnList.add(m);
        }
        taskPageData.setContent(returnList);
        return taskPageData;
    }

    /**
     * 返回流程定义的分页数据
     *
     * @author ln
     * @since 2016年9月14日
     * @version 
     * @param pagenum
     * @param pagesize
     * @return
     * @return: PageData<Map<String,Object>>
     */
    public PageData<Map<String,Object>> getProcessDefPage(int pagenum, int pagesize, String key, String name) {
        PageData<Map<String,Object>>processDefData = new PageData<Map<String,Object>>();
        List<ProcessDefinition> list = null;
        list = repositoryService.createProcessDefinitionQuery()
                .processDefinitionNameLike("%" + name.trim() + "%")
                .processDefinitionKeyLike("%"+ key.trim() + "%")
                .orderByProcessDefinitionKey().asc()
                .orderByProcessDefinitionVersion().desc().listPage(pagenum * pagesize, pagesize);
            processDefData.setTotalElements(repositoryService.createProcessDefinitionQuery()
                .processDefinitionNameLike("%" + name.trim() + "%")
                .processDefinitionKeyLike("%" + key.trim() + "%").count());            
        List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>(); 
        for(ProcessDefinition p: list){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", p.getId());
            map.put("key", p.getKey());
            map.put("name", p.getName());
            map.put("version", p.getVersion());
            map.put("diagramResourceName", p.getDiagramResourceName());
            map.put("resourceName", p.getResourceName());
            map.put("handleHref", "<a href='javascript:void(0)' onclick='showProcessChart()'>查看流程图</a>");
            //map.put("handleHref", "<a href=\"graphprocessdefinition?processDefinitionId=" + p.getId() + "\" target=\"_blank\">查看流程图</a>");
            //System.out.println("<a href=\"graphprocessdefinition?processDefinitionId=" + p.getId() + "\" target=\"_blank\"><spring:message code=\"biz.hr.label.viewProcessChart\"/></a>");
            returnList.add(map);
        }
        processDefData.setContent(returnList);
        return processDefData;
    }

    /**
     * 获取发布的模型
     *
     * @author ln
     * @since 2016年9月20日
     * @version 
     * @param pagenum
     * @param pagesize
     * @param name
     * @return
     * @return: PageData<Map<String,Object>>
     */
    public PageData<Map<String, Object>> getReModel(int pagenum, int pagesize, String name) {
        PageData<Map<String,Object>>processDefData = new PageData<Map<String,Object>>();
        List<Model> list = null;
        list = repositoryService.createModelQuery()
                .modelNameLike("%" + name.trim() + "%")
                .orderByLastUpdateTime()
                .desc()
                .listPage(pagenum * pagesize, pagesize);
            processDefData.setTotalElements(repositoryService.createModelQuery()
                    .modelNameLike("%" + name.trim() + "%").count());            
        List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>(); 
        for(Model m: list){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", m.getId());
            map.put("key", m.getKey());
            map.put("name", m.getName());
            map.put("version", m.getVersion());
            map.put("createTime", m.getCreateTime());
            map.put("lastUpdateTime", m.getLastUpdateTime());
            map.put("deploymentId", m.getDeploymentId());
            map.put("handleHref", "<a href='javascript:void(0)' onclick='modifyModel()'>修改</a> <a href='javascript:void(0)' onclick='removeModel()'>删除</a> <a href='javascript:void(0)' onclick='deployModel()'>发布</a>" );
            //System.out.println("<a href=\"graphprocessdefinition?processDefinitionId=" + p.getId() + "\" target=\"_blank\"><spring:message code=\"biz.hr.label.viewProcessChart\"/></a>");
            returnList.add(map);
        }
        processDefData.setContent(returnList);
        return processDefData;
    }    
}
