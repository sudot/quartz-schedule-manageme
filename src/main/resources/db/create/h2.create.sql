-- 任务配置表
CREATE TABLE job_config
(
    id         bigint IDENTITY PRIMARY KEY NOT NULL,
    key        varchar(100)                NOT NULL,
    name       varchar(200)                NOT NULL,
    state      varchar(20)                 NOT NULL,
    cron       varchar(20)                 NOT NULL,
    class_name varchar(250)                NOT NULL,
);
comment on table job_config is '任务配置表';
comment on column job_config.id is '主键';
comment on column job_config.key is '任务编号,必须唯一';
comment on column job_config.name is '任务名称';
comment on column job_config.state is '任务状态';
comment on column job_config.cron is 'cron表达式';
comment on column job_config.class_name is '类全限定名(含包名)';
