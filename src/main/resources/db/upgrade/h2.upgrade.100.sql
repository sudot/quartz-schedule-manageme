-- 添加cron表达式字段
ALTER TABLE job_config ADD COLUMN class_name varchar(250) not null;
comment on column job_config.class_name is '类全限定名(含包名)';
