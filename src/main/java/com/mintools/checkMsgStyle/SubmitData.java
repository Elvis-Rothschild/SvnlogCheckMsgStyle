package com.mintools.checkMsgStyle;

public class SubmitData
{
	private String revision = "-";
	private String author = "-";
	private String submitDate = "-";
	private boolean need = false;
	private StringBuffer NonCompliantTxt = null;

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(String submitDate) {
		this.submitDate = submitDate;
	}

	public SubmitData()
	{
		NonCompliantTxt = new StringBuffer();
	}
	
	public SubmitData(String author)
	{
		this.author = author;
	}

	public String getText()
	{
		return NonCompliantTxt.toString();
	}

	public StringBuffer getTextBuffer()
	{
		return NonCompliantTxt;
	}

	public void addTxt(String str)
	{
		need = true;
		NonCompliantTxt.append(str);
		NonCompliantTxt.append(CommonConstants.LINE_SEPARATOR);
	}

	public void noBugId()
	{
		this.addTxt("[问题单号]未填写！");
	}

	public void noBugIdLength()
	{
		this.addTxt("[问题单号]信息不全，太简单！");
	}

	public void noBugDESC()
	{
		this.addTxt("[问题描述]未填写！");
	}

	public void noBugDESCLength()
	{
		this.addTxt("[问题描述]信息不全，太简单！");
	}

	public void noBugREASON()
	{
		this.addTxt("[问题原因]未填写！");
	}

	public void noBugREASONLength()
	{
		this.addTxt("[问题原因]信息不全，太简单！");
	}

	public void noModify()
	{
		this.addTxt("[修改描述]未填写！");
	}
	public void noModifyLength()
	{
		this.addTxt("[修改描述]信息不全，太简单！");
	}

	public void noInfluence()
	{
		this.addTxt("[影响域]未填写！");
	}
	public void noInfluenceLength()
	{
		this.addTxt("[影响域]信息不全，太简单！");
	}

	public void noReviser()
	{
		this.addTxt("[修改人]未填写！");
	}

	public void noReviserLength()
	{
		this.addTxt("[修改人]信息不全，太简单！");
	}

	public void noReviewer()
	{
		this.addTxt("[审核人]未填写！");
	}

	public void noReviewerLength()
	{
		this.addTxt("[审核人]信息不全，太简单！");
	}
	
	public void unvalidMofier()
	{
		this.addTxt("[提交人]和[修改人]不符合");
	}

	public void noTime()
	{
		this.addTxt("[修改时间]未填写！");
	}

	public void noTimeLength()
	{
		this.addTxt("[修改时间]信息不全，太简单！");
	}

	public void noInvalid()
	{
		this.addTxt("[是否本地验证通过]未填写！");
	}

	public void noInvalidLength()
	{
		this.addTxt("[是否本地验证通过]信息不全，太简单！");
	}

	public void noInvalidOK()
	{
		this.addTxt("[是否本地验证通过]未本地验证！");
	}


	public String toString()
	{
		StringBuffer bf = new StringBuffer();
		bf.append("提交人："+author);
		bf.append("/节点号：");
		bf.append(revision);
		bf.append("/提交时间：");
		bf.append(submitDate);
		
		bf.append(CommonConstants.LINE_SEPARATOR);
		bf.append(NonCompliantTxt);
		
		return bf.toString();
	}
	
	public boolean valid()
	{
		return this.need;
	}
}
