package com.mintools.checkMsgStyle;

import cn.hutool.core.date.DateUtil;
import com.mintools.sendMailUtil.SendEmail;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.*;

public class SvnlogCheckMsgStyleMain {

    private int count = 0;
    private int total = 0;
    private static int countAll = 0;
    private static int totalAll = 0;
    private static File logFile = null;

    private SubmitData submitData = null;

    private HashMap<String, ArrayList<SubmitData>> mapRecord = new HashMap<String, ArrayList<SubmitData>>();
    private ArrayList<SubmitData> listRecord = null;

    private Document document = null; /*日志xml文档*/

    private HashMap<String, String> msgInfo = null;

    // 邮件正文
    private static String content = "";
    private static String contentDelay = "";
    private static String dateStart;
    private static boolean isDateEnd = false;
    private static String dateEnd = "（可选）";
    private static String hdCodePath;


    public static void main(String[] args) {
        if (args.length <= 0) {
            System.out.println("参数错误！");
            System.out.println("参数错误！");
            System.out.println("参数错误！");
            return;
        }

        // fileNum [ file1 file2 file3 ... ] dateStart (dateEnd)
        // 文件个数 [ 文件1 文件2 文件3 ... ] 开始日期 结束日期(可选)
        // 文件名建议使用代码路径命名

        int fileNum = Integer.parseInt(args[0]);
        if (fileNum < 1) {
            System.out.println("参数错误！");
            System.out.println("参数错误！");
            System.out.println("参数错误！");
            return;
        }
        if (1 + fileNum + 1 == args.length || 1 + fileNum + 2 == args.length) {
            dateStart = args[1 + fileNum];
            if (1 + fileNum + 2 == args.length) {
                isDateEnd = true;
                dateEnd = args[1 + fileNum + 1];
            }
        }

        for (int i = 1; i < 1 + fileNum; i++) {
            String fileName = args[i];
            int suffix = fileName.lastIndexOf(".");
            if (suffix == -1 || suffix == 0) {
                System.out.println("参数错误！");
                System.out.println("参数错误！");
                System.out.println("参数错误！");
                return;
            }
            hdCodePath = fileName.substring(0, suffix);

            logFile = new File(fileName);
            if (!logFile.exists()) {
                System.out.println("文件不存在！");
                return;
            }

            new SvnlogCheckMsgStyleMain().dealLog();
//			dealLog();
        }

        // 输出结果排序：有疑似不合规数的优先展示
        content+=contentDelay;

        // 输出结果汇总：统计所有审计文件的数据
        content = "<h3>总审计代码路径数：" + fileNum + "</h3>"
                + "<h3>所有审计代码路径的总提交数：" + totalAll + "</h3>"
                + "<h3>所有审计代码路径的疑似不合规数：" + countAll + "</h3>"
                + content;

        SendEmail.sendEmail(content);
    }


    //处理xml日志文本
    public void dealLog() {
        try {
//			System.out.println("读取日志文件！");
            SAXReader reader = new SAXReader();
            this.document = reader.read(logFile);

//			System.out.println("统计日志信息！");
            Element e = this.document.getRootElement();
            dealElement(e);

            printInfo();

//			System.out.println("完成！");
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
        } catch (DocumentException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException ew) {
            // TODO Auto-generated catch block
            ew.printStackTrace();
        }
    }


    private void dealElement(Element node) throws IOException {
        readElement(node);

        Iterator<Element> it = node.elementIterator();
        while (it.hasNext()) {
            // 获取某个子节点对象
            Element e = it.next();
            // 对子节点进行遍历
            dealElement(e);
        }
    }

    private void readElement(Element node) throws IOException {
        String eName = node.getName();
        switch (eName) {
            case "logentry":
                total++;
                this.submitData = new SubmitData();    //新纪录开始，初始化数据
                this.submitData.setRevision(node.attribute("revision").getValue());
                break;
            case "author":
                this.submitData.setAuthor(node.getText());
                break;
            case "date":
                this.submitData.setSubmitDate(node.getText());
                break;
            case "msg":
                // TODO 校验、排除svn.log中提交日期早于起始时间的冗余数据
                String submitDateStr = submitData.getSubmitDate().substring(0, 10);
                Date submitDate = DateUtil.parse(submitDateStr);
                Date startDate = DateUtil.parse(dateStart);
//				Date endDate = DateUtil.parse(dateEnd);
                if (submitDate.getTime() < startDate.getTime()) {
                    submitData = null;
                    total--;
                    return;
                }

                // 处理提交日志是否符合规范
                dealMsg(node.getText());
                // 保存数据
                updateData();
                break;
            default:
                break;
        }
    }

    //处理提交代码填写的信息
    private void dealMsg(String msg) throws IOException {
        formatMsg(msg);

        dealMsgAuthor();

        dealMsgBasicInfo();

        dealMsgModifyInfo();

        if (submitData.valid()) {
            submitData.addTxt("+++完整提交记录如下+++");
            submitData.addTxt(msg);
        }
    }

    private void formatMsg(String msg) throws IOException {
        msgInfo = new HashMap<String, String>();
        StringReader sR = new StringReader(msg);
        BufferedReader bfR = new BufferedReader(sR);

        String strLine = null;
        //System.out.println("++++++++++++++++++++++");
        while ((strLine = bfR.readLine()) != null) {
            int index = strLine.indexOf("：");
            String key = null;
            String value = null;
            if (index != -1) {
                key = strLine.substring(0, index);
                value = strLine.substring(index + 1);
            }

            if (invalid(key)) {
                index = strLine.indexOf(":");
                if (index != -1) {
                    key = strLine.substring(0, index);
                    value = strLine.substring(index + 1);
                }
            }

//			System.out.println("+++key+++"+key+"+++value+++"+value);

            msgInfo.put(key, value);
        }

        //printMap();
    }

    private void dealMsgAuthor() {

    }

    private void dealMsgBasicInfo() {
        // 问题单号
        String bugId = msgInfo.get(CommonConstants.SUBMIT_DESC_BUGID);
        if (invalid(bugId) || containNA(bugId)) {
            // TODO
            submitData.noBugId();
            // return;
        } else if (invalidLength(bugId, 1)) {
            submitData.noBugIdLength();
        }

        // 问题描述
        String bugDesc = msgInfo.get(CommonConstants.SUBMIT_DESC_BUGDESC);
        if (invalid(bugDesc)) {
//			submitData.addTxt("问题描述+++"+bugDesc);
            submitData.noBugDESC();
        } else if (invalidLength(bugDesc, 6)) {
//			submitData.addTxt("问题描述+++"+bugDesc);
            submitData.noBugDESCLength();
        }

        // 问题原因
        String bugReason = msgInfo.get(CommonConstants.SUBMIT_DESC_BUGREASON);
        if (invalid(bugReason)) {
//			submitData.addTxt("问题原因+++"+bugReason);
            submitData.noBugREASON();
        } else if (invalidLength(bugReason, 6)) {
//			submitData.addTxt("问题原因+++"+bugReason);
            submitData.noBugREASONLength();
        }

    }

    private void dealMsgModifyInfo() {
        // 修改描述
        String bugModifyDesc = msgInfo.get(CommonConstants.SUBMIT_DESC_MODIFYDESC);
        if (invalid(bugModifyDesc)) {
//			submitData.addTxt("修改描述+++"+bugModifyDesc);
            submitData.noModify();
        } else if (invalidLength(bugModifyDesc, 6)) {
//			submitData.addTxt("修改描述+++"+bugModifyDesc);
            submitData.noModifyLength();
        }

        // 影响域
        String bugInfluence = msgInfo.get(CommonConstants.SUBMIT_DESC_INFLUENCE);
        if (invalid(bugInfluence)) {
//			submitData.addTxt("影响域+++"+bugInfluence);
            submitData.noInfluence();
        } else if (invalidLength(bugInfluence, 2)) {
//			submitData.addTxt("影响域+++"+bugInfluence);
            submitData.noInfluenceLength();
        }

        // 修改人
        String bugReviser = msgInfo.get(CommonConstants.SUBMIT_DESC_REVISER);
        if (invalid(bugReviser)) {
//			submitData.addTxt("修改人+++"+bugReviser);
            submitData.noReviser();
        } else if (invalidLength(bugReviser, 2)) {
//			submitData.addTxt("修改人+++"+bugReviser);
            submitData.noReviserLength();
        }

        // 审核人
        String bugReviewer = msgInfo.get(CommonConstants.SUBMIT_DESC_REVIEWER);
        if (invalid(bugReviewer)) {
//			submitData.addTxt("审核人+++"+bugReviewer);
            submitData.noReviewer();
        } else if (invalidLength(bugReviewer, 2)) {
//			submitData.addTxt("审核人+++"+bugReviewer);
            submitData.noReviewerLength();
        }

        // 修改时间
        String bugTime = msgInfo.get(CommonConstants.SUBMIT_DESC_MODITIME);
        if (invalid(bugTime)) {
//			submitData.addTxt("修改时间+++"+bugTime);
            submitData.noTime();
        } else if (invalidLength(bugTime, 8)) {
//			submitData.addTxt("修改时间+++"+bugTime);
            submitData.noTimeLength();
        }

        // 本地验证
        String bugInvalid = msgInfo.get(CommonConstants.SUBMIT_DESC_VERIFICATION);
        if (invalid(bugInvalid)) {
//			submitData.addTxt("本地验证+++"+bugInvalid);
            submitData.noInvalid();
        } else if (invalidLength(bugInvalid, 1)) {
//			submitData.addTxt("本地验证+++"+bugInvalid);
            submitData.noInvalidLength();
        } else if (!bugInvalid.contains(CommonConstants.SUBMIT_DESC_VERIFICATION_YES)) {
//			submitData.addTxt("本地验证+++"+bugInvalid);
            submitData.noInvalidOK();
        }
    }

    //判断字符串是否无效
    private boolean invalid(String str) {
        return str == null || "".equals(str);
    }

    private boolean invalidLength(String str, int length) {
        return str == null || "".equals(str) || str.length() < length;
    }

    private boolean containNA(String str) {

        int i = str.indexOf("NA");
        int j = str.indexOf("N/A");

        return i > -1 || j > -1;
    }


    //保存整理后数据
    private void updateData() {
        if (!submitData.valid()) {
            return;
        }

        count++;

        listRecord = this.mapRecord.get(submitData.getAuthor());
        if (listRecord == null) {
            listRecord = new ArrayList<SubmitData>();
            mapRecord.put(submitData.getAuthor(), listRecord);
        }
        listRecord.add(submitData);
        submitData = null;
    }

    //打印整个保存的数据
    public void printInfo() {
        //打印统计信息
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("总提交数：" + this.total);
        System.out.println("疑似不合规数：" + this.count);
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
        totalAll += total;
        countAll += count;

        if (this.count != 0) {
            content += "<hr>";
        } else {
            contentDelay += "<hr>";
        }

        if (isDateEnd) {
            if (this.count != 0) {
                content += "<h3>上库规范性审计日期：" + dateStart + "至" + dateEnd + "</h3>";
            } else {
                contentDelay += "<h3>上库规范性审计日期：" + dateStart + "至" + dateEnd + "</h3>";
            }
        } else {
            if (this.count != 0) {
                content += "<h3>上库规范性审计日期：" + dateStart + "</h3>";
            } else {
                contentDelay += "<h3>上库规范性审计日期：" + dateStart + "</h3>";
            }
        }

        if (this.count != 0) {
            content += "<h3>检查文件（代码路径）：" + hdCodePath + "</h3>";
            content += "<h3>总提交数：" + this.total + "</h3>";
            content += "<h3>疑似不合规数：" + this.count + "</h3>";
//			content+="<hr>";
        } else {
            contentDelay += "<h3>检查文件（代码路径）：" + hdCodePath + "</h3>";
            contentDelay += "<h3>总提交数：" + this.total + "</h3>";
            contentDelay += "<h3>疑似不合规数：" + this.count + "</h3>";
        }

        //按人打印详细的不合规信息
        Set<String> set = mapRecord.keySet();
        if (set.size() != 0) {
            System.out.println("详细信息：");
//			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
            if (this.count != 0) {
                content += "<h3>详细信息：</h3>";
            } else {
                contentDelay += "<h3>详细信息：</h3>";
            }
        }
        Iterator<String> its = set.iterator();
        while (its.hasNext()) {
            String name = its.next();
            System.out.println("--------------------");
            if (this.count != 0) {
                content += "<h4>--------------------</h4>";
            } else {
                contentDelay += "<h4>--------------------</h4>";
            }
            String name_zh_CN = null;
//			switch (name)
//			{
//				case "guochun.xiao" :
//					name_zh_CN = "肖国春";
//					break;
//				case "liang.zhou" :
//					name_zh_CN = "周亮";
//					break;
//				case "xiaobo.wang" :
//					name_zh_CN = "王晓波";
//					break;
//				default:
//					name_zh_CN = name;
//			}
            name_zh_CN = getUserNameProperties(name);
            System.out.println("提交人：" + name_zh_CN);
            ArrayList<SubmitData> array = mapRecord.get(name);
            System.out.println("不合规数：" + array.size());
            System.out.println("不合规记录信息：");
            if (this.count != 0) {
                content += "<h4>提交人：" + name_zh_CN + "</h4>";
                content += "<h4>不合规数：" + array.size() + "</h4>";
                content += "<h4>不合规记录信息：</h4>";
            } else {
                contentDelay += "<h4>提交人：" + name_zh_CN + "</h4>";
                contentDelay += "<h4>不合规数：" + array.size() + "</h4>";
                contentDelay += "<h4>不合规记录信息：</h4>";
            }
            Iterator<SubmitData> ita = array.iterator();
            while (ita.hasNext()) {
                SubmitData data = ita.next();
                System.out.println(data.toString());
                if (this.count != 0) {
                    content += "<h5>" + data.toString() + "</h5>";
                } else {
                    contentDelay += "<h5>" + data.toString() + "</h5>";
                }
            }
        }
    }

    private String getUserNameProperties(String name) {
        Properties pro = new Properties();
        InputStream is = null;
        String nameStr = null;
        try {
//			is = new PropertyRead().getClass().getResourceAsStream("/userName.properties");
            is = SvnlogCheckMsgStyleMain.class.getResourceAsStream("/userName.properties");
            // 读取users.properties
            pro.load(is);
            nameStr = pro.getProperty(name);
        } catch (IOException e) {
            nameStr = name;
            System.err.println("读取userName.properties文件失败   " + e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                System.err.println("reading from userName.properties get io exception   " + e);
            }
        }

/*
		//输出属性文件中的信息
		Set set = props.keySet();
		Iterator it = set.iterator();
		try {
			while (it.hasNext()) {
				String key = (String) it.next();
				if (key.equals(name)) {
					result = new String(props.getProperty(key).getBytes("ISO-8859-1"),"utf-8");
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/

        if (nameStr == null || "".equals(nameStr)) {
            nameStr = name;
        }
        return nameStr;
    }

    public void printMap() {
        Set<String> set = msgInfo.keySet();
        Iterator<String> its = set.iterator();

        while (its.hasNext()) {
            String key = its.next();
            String value = msgInfo.get(key);
            System.out.println(key + value);
        }

    }

}
