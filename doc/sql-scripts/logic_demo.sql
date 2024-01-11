/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 80200
 Source Host           : localhost:3306
 Source Schema         : logic_demo

 Target Server Type    : MySQL
 Target Server Version : 80200
 File Encoding         : 65001

 Date: 16/11/2023 18:22:10
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for logic
-- ----------------------------
DROP TABLE IF EXISTS `logic`;
CREATE TABLE `logic` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `configJson` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for logic_bak
-- ----------------------------
DROP TABLE IF EXISTS `logic_bak`;
CREATE TABLE `logic_bak` (
  `aid` bigint NOT NULL AUTO_INCREMENT,
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `configJson` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`aid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1725072157574766595 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for logic_instance
-- ----------------------------
DROP TABLE IF EXISTS `logic_instance`;
CREATE TABLE `logic_instance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `logicId` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '逻辑编号',
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '逻辑版本',
  `bizId` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '业务实例标识',
  `nextId` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '待执行节点',
  `nextName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息',
  `messageId` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息标识',
  `paramsJson` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '缓存前序流程的入参值',
  `varsJson` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '开始执行时的局部变量值',
  `varsJsonEnd` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '执行结束时局部变量值，为下次交互时的恢复变量值',
  `returnData` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '最近一次返回数据',
  `env` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '环境',
  `success` tinyint(1) DEFAULT NULL COMMENT '最近一次是否成功',
  `isOver` tinyint(1) DEFAULT '0' COMMENT '是否结束',
  `serverTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '服务器时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1725095028881190915 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for logic_log
-- ----------------------------
DROP TABLE IF EXISTS `logic_log`;
CREATE TABLE `logic_log` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `env` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '运行时节点环境标识',
  `logicId` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '逻辑编号',
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '逻辑版本',
  `bizId` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '业务实例标识',
  `clientId` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求执行客户端标识',
  `host` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求执行的host',
  `success` tinyint(1) DEFAULT NULL COMMENT '是否执行成功',
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '返回消息',
  `nextId` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '待执行节点编号',
  `nextName` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '待执行节点名称',
  `serverTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '服务器时间',
  `paramsJson` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '当前请求的参数记录',
  `varsJson` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '开始执行时的局部变量',
  `varsJsonEnd` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '执行完成后的局部变量，用于下一个交互的执行',
  `returnData` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '返回数据',
  `isOver` tinyint(1) DEFAULT '0' COMMENT '是否已经执行到最后一个节点',
  `itemLogs` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '执行过程日志',
  `messageId` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息唯一标识',
  PRIMARY KEY (`id`,`version`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for logic_published
-- ----------------------------
DROP TABLE IF EXISTS `logic_published`;
CREATE TABLE `logic_published` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `logicId` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `configJson` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `publishTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
