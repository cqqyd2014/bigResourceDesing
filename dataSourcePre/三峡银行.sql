
/****** Script for SelectTopNRows command from SSMS  ******/
--账号汇总表

drop table audit_account
go
select 账号,账户名称,客户号,'对公' 类型 into audit_account
 from [dbo].[基础表_存款业务_对公活期存款分户账] where 时点日期='2018-01-31' 
 go
 insert into audit_account  select 账号,账户名称,客户号 ,'对私'
 from [dbo].[基础表_存款业务_个人活期存款分户账]
 go
 create index idx_audit_account on audit_account(账号,类型)
 go

 drop table audit_account_trans
 go

SELECT  d.证件号码,
b.[账号]

      ,CONVERT(DATEtime,REPLACE(交易日期,'-','')+' '+case when len(b.交易时间戳)=1 then '0'+交易时间戳+':00:00' 
     WHEN LEN(b.交易时间戳)=2 THEN '0'+LEFT(交易时间戳,1)+':0'+SUBSTRING(交易时间戳,2,1)+':00' 
	 WHEN LEN(b.交易时间戳)=3 THEN '0'+LEFT(交易时间戳,1)+':0'+SUBSTRING(交易时间戳,2,1)+':0'+SUBSTRING(交易时间戳,3,1) 
	 WHEN LEN(b.交易时间戳)=4 THEN '0'+LEFT(交易时间戳,1)+':0'+SUBSTRING(交易时间戳,2,1)+':'+SUBSTRING(交易时间戳,3,2)
	 WHEN LEN(b.交易时间戳)=5 THEN '0'+LEFT(交易时间戳,1)+':'+SUBSTRING(交易时间戳,2,2)+':'+SUBSTRING(交易时间戳,4,2) 
	 WHEN LEN(b.交易时间戳)=6 THEN LEFT(交易时间戳,2)+':'+SUBSTRING(交易时间戳,3,2)+':'+SUBSTRING(交易时间戳,5,2) 
	 end,121) 交易时间         ,[交易金额]      ,[对方账号] 对手账号
      ,[对方账户名称] 对手户名      ,[对方行名称] 对手行名     
      ,[子交易流水] 流水号      ,[摘要码] 摘要     
     ,c.执照号码 对手统一社会编码
	 into audit_account_trans
  FROM [sbdet].[dbo].[基础表_存款业务_个人活期存款明细账] b,[dbo].[audit_account] a
,[dbo].[基础表_客户信息_对公客户扩展信息表] c
,[sbdet].[dbo].[基础表_客户信息_对私客户基本信息表] d
,[dbo].[基础表_存款业务_个人活期存款分户账] e
where 对方账号=a.账号 and a.客户号=c.客户号 and len(c.执照号码)=18
and d.证件类型 in ('Ind01','1') 
--and len(d.证件号码)=18 
and e.账号=b.账号 and e.客户号=d.客户号