package com.zk.monitor.modules.data.tenant;

import com.baomidou.mybatisplus.extension.plugins.tenant.TenantHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * com.zk.monitor.modules.data.tenant
 * create by admin nihui
 * create time 2020/11/12
 * version 1.0
 **/
@Slf4j
@Component
public class PreTenantHandler implements TenantHandler {

    @Autowired
    private PreTenantConfigProperties configProperties;

    /**
     * 租户Id
     *
     * @return
     */
    @Override
    public Expression getTenantId(boolean where) {
        Long tenantId = PreTenantContextHolder.getCurrentTenantId();
        log.debug("当前租户为{}", tenantId);
        if (tenantId == null) {
            return new NullValue();
        }
        return new LongValue(tenantId);
    }
    /**
     * 租户字段名
     *
     * @return
     */
    @Override
    public String getTenantIdColumn() {
        return configProperties.getTenantIdColumn();
    }

    /**
     * 根据表名判断是否进行过滤
     * 忽略掉一些表：如租户表（sys_tenant）本身不需要执行这样的处理
     *
     * @param tableName
     * @return
     */
    @Override
    public boolean doTableFilter(String tableName) {
        return configProperties.getIgnoreTenantTables().stream().anyMatch((e) -> e.equalsIgnoreCase(tableName));
    }
}

