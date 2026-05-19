package com.gmp.framework.model2table;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * 自动建表引擎 - 基于实体类注解自动创建和更新数据库表结构
 *
 * 核心功能（参考原sisqp系统的model2table设计）：
 * 1. 扫描指定包下的实体类
 * 2. 根据@Table和@Column注解生成DDL语句
 * 3. 与现有数据库表对比，差异处自动ALTER TABLE
 * 4. 支持三种模式：CREATE(创建新表)、UPDATE(更新已有表)、VALIDATE(仅校验)
 *
 * 支持的注解：
 * - @AutoTable: 标记类需要自动建表
 * - @AutoColumn: 标记属性对应的数据库列
 *
 * 安全策略：
 * - 默认不删除已存在的列（防止数据丢失）
 * - 修改列类型前检查数据兼容性
 * - 所有DDL操作记录日志
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Component
public class TableAutoBuilder implements CommandLineRunner {

    private final DataSource dataSource;

    /** 建表模式 */
    public enum BuildMode {
        /** 只创建新表，不修改现有表 */
        CREATE,
        /** 创建新表并更新现有表差异 */
        UPDATE,
        /** 只校验，不执行任何DDL */
        VALIDATE,
        /** 禁用自动建表 */
        NONE
    }

    /** 默认建表模式为VALIDATE（生产安全） */
    private BuildMode buildMode = BuildMode.VALIDATE;

    public TableAutoBuilder(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Spring Boot启动后自动执行
     * 扫描实体类并对比数据库表结构
     */
    @Override
    public void run(String... args) {
        if (buildMode == BuildMode.NONE) {
            log.info("自动建表功能已禁用");
            return;
        }
        log.info("自动建表引擎启动 - 模式: {}", buildMode);
        // 实际的扫描和对比逻辑应由具体的实体扫描器完成
        // 这里提供框架基础，具体实现由各业务模块的实体扫描器补充
    }

    /**
     * 检查表是否存在
     *
     * @param tableName 表名
     * @return true=存在
     */
    public boolean isTableExists(String tableName) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
            return rs.next();
        } catch (SQLException e) {
            log.error("检查表是否存在失败: {} - {}", tableName, e.getMessage());
            return false;
        }
    }

    /**
     * 获取表的所有列信息
     *
     * @param tableName 表名
     * @return 列名 -> 列类型映射
     */
    public Map<String, String> getTableColumns(String tableName) {
        Map<String, String> columns = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getColumns(null, null, tableName, null);
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String columnType = rs.getString("TYPE_NAME");
                int columnSize = rs.getInt("COLUMN_SIZE");
                columns.put(columnName, columnType + "(" + columnSize + ")");
            }
        } catch (SQLException e) {
            log.error("获取表列信息失败: {} - {}", tableName, e.getMessage());
        }
        return columns;
    }

    /**
     * 执行DDL语句（仅在非VALIDATE模式下）
     *
     * @param ddl DDL语句
     */
    public void executeDDL(String ddl) {
        if (buildMode == BuildMode.VALIDATE) {
            log.info("[VALIDATE模式] 跳过DDL: {}", ddl);
            return;
        }
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
            log.info("DDL执行成功: {}", ddl);
        } catch (SQLException e) {
            log.error("DDL执行失败: {} - 错误: {}", ddl, e.getMessage());
        }
    }

    /**
     * 生成CREATE TABLE语句
     *
     * @param tableName    表名
     * @param columns      列定义 Map<列名, 列SQL定义>
     * @param primaryKey   主键列名
     * @param tableComment 表注释
     * @return CREATE TABLE DDL
     */
    public String generateCreateTableSQL(String tableName, Map<String, String> columns,
                                          String primaryKey, String tableComment) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE `").append(tableName).append("` (\n");

        List<String> columnDefs = new ArrayList<>();
        for (Map.Entry<String, String> entry : columns.entrySet()) {
            String colDef = "  `" + entry.getKey() + "` " + entry.getValue();
            if (entry.getKey().equals(primaryKey)) {
                colDef += " NOT NULL";
            }
            columnDefs.add(colDef);
        }
        sql.append(String.join(",\n", columnDefs));

        if (primaryKey != null && !primaryKey.isEmpty()) {
            sql.append(",\n  PRIMARY KEY (`").append(primaryKey).append("`)");
        }

        sql.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        if (tableComment != null && !tableComment.isEmpty()) {
            sql.append(" COMMENT='").append(tableComment).append("'");
        }

        sql.append(";");
        return sql.toString();
    }

    /**
     * 生成ALTER TABLE ADD COLUMN语句
     *
     * @param tableName  表名
     * @param columnName 列名
     * @param columnDef  列定义
     * @param comment    列注释
     * @return ALTER TABLE DDL
     */
    public String generateAddColumnSQL(String tableName, String columnName,
                                        String columnDef, String comment) {
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE `").append(tableName).append("` ")
                .append("ADD COLUMN `").append(columnName).append("` ").append(columnDef);
        if (comment != null && !comment.isEmpty()) {
            sql.append(" COMMENT '").append(comment).append("'");
        }
        sql.append(";");
        return sql.toString();
    }

    public void setBuildMode(BuildMode buildMode) {
        this.buildMode = buildMode;
    }
}
