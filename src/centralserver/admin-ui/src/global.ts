/*
 * The MIT License
 * Copyright (c) 2019- Nordic Institute for Interoperability Solutions (NIIS)
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
import { Tab } from '@/ui-types';

// Vuex Store root state
export interface RootState {
  version: string;
}

// Vuex store types
export const StoreTypes = {
  getters: {
    // User
    IS_AUTHENTICATED: 'IS_AUTHENTICATED',
    IS_SESSION_ALIVE: 'IS_SESSION_ALIVE',
    USERNAME: 'USERNAME',
    HAS_PERMISSION: 'HAS_PERMISSION',
    HAS_ANY_OF_PERMISSIONS: 'HAS_ANY_OF_PERMISSIONS',
    GET_ALLOWED_TABS: 'GET_ALLOWED_TABS',
    FIRST_ALLOWED_TAB: 'FIRST_ALLOWED_TAB',
    // Notifications
    SUCCESS_NOTIFICATIONS: 'SUCCESS_NOTIFICATIONS',
    ERROR_NOTIFICATIONS: 'ERROR_NOTIFICATIONS',
    CONTINUE_INIT: 'CONTINUE_INIT',
    // Tokens
    TOKENS: 'TOKENS',
    TOKEN_EXPANDED: 'TOKEN_EXPANDED',
    SORTED_TOKENS: 'SORTED_TOKENS',
    SELECTED_TOKEN: 'SELECTED_TOKEN',
    // Initialization
    IS_SERVER_INITIALIZED: 'IS_SERVER_INITIALIZED',
    // System
    SERVER_VERSION: 'SERVER_VERSION',
    SYSTEM_STATUS: 'SYSTEM_STATUS',
  },
  mutations: {
    // User
    SET_SESSION_ALIVE: 'SET_SESSION_ALIVE',
    SET_USERNAME: 'SET_USERNAME',
    CLEAR_AUTH_DATA: 'CLEAR_AUTH_DATA',
    AUTH_USER: 'AUTH_USER',
    SET_PERMISSIONS: 'SET_PERMISSIONS',
    // Notifications
    SET_NOTIFICATIONS_DEFAULT_STATE: 'SET_NOTIFICATIONS_DEFAULT_STATE',
    SET_SUCCESS: 'SET_SUCCESS',
    SET_ERROR_MESSAGE: 'SET_ERROR_MESSAGE',
    SET_ERROR_ACTION: 'SET_ERROR_ACTION',
    SET_ERROR_OBJECT: 'SET_ERROR_OBJECT',
    DELETE_SUCCESS_NOTIFICATION: 'DELETE_SUCCESS_NOTIFICATION',
    DELETE_NOTIFICATION: 'DELETE_NOTIFICATION',
    SET_CONTINUE_INIT: 'SHOW_CONTINUE_INIT',
    // Tokens
    SET_TOKEN_HIDDEN: 'SET_TOKEN_HIDDEN',
    SET_TOKEN_EXPANDED: 'SET_TOKEN_EXPANDED',
    SET_TOKENS: 'SET_TOKENS',
    SET_SELECTED_TOKEN: 'SET_SELECTED_TOKEN',
    // Initialization
    // System
    SET_SERVER_VERSION: 'SET_SERVER_VERSION',
    SET_SYSTEM_STATUS: 'SET_SYSTEM_STATUS',
  },
  actions: {
    // User
    LOGIN: 'LOGIN',
    LOGOUT: 'LOGOUT',
    CLEAR_AUTH: 'CLEAR_AUTH',
    FETCH_SESSION_STATUS: 'FETCH_SESSION_STATUS',
    FETCH_USER_DATA: 'FETCH_USER_DATA',
    // Notifications
    RESET_NOTIFICATIONS_STATE: 'RESET_NOTIFICATIONS_STATE',
    SHOW_ERROR: 'SHOW_ERROR', // Show error notification with axios error object
    SHOW_SUCCESS: 'SHOW_SUCCESS', // Show success notification with text string
    SHOW_ERROR_MESSAGE: 'SHOW_ERROR_MESSAGE', // Show error notification with text string
    // Initialization
    INITIALIZATION_REQUEST: 'INITIALIZATION_REQUEST',
    // System
    FETCH_SERVER_VERSION: 'FETCH_SERVER_VERSION',
    FETCH_SYSTEM_STATUS: 'FETCH_SYSTEM_STATUS',
    UPDATE_CENTRAL_SERVER_ADDRESS: 'UPDATE_CENTRAL_SERVER_ADDRESS',
  },
};

// A "single source of truth" for route names
export enum RouteName {
  BaseRoute = 'base',
  Members = 'members',
  MemberDetails = 'member-details',
  MemberManagementRequests = 'member-management-requests',
  MemberSubsystems = 'member-subsystems',
  SecurityServers = 'security-servers',
  ManagementRequests = 'management-requests',
  TrustServices = 'trust-services',
  Settings = 'settings',
  GlobalResources = 'global-resources',
  GlobalGroup = 'global-group',
  SystemSettings = 'system-settings',
  BackupAndRestore = 'backup-and-restore',
  ApiKeys = 'api-keys',
  CreateApiKey = 'create-api-key',
  InternalConfiguration = 'internal-configuration',
  ExternalConfiguration = 'external-configuration',
  TrustedAnchors = 'trusted-anchors',
  Login = 'login',
  Initialisation = 'init',
  SecurityServerDetails = 'security-server-details',
  SecurityServerManagementRequests = 'security-server-management-requests',
  SecurityServerAuthenticationCertificates = 'security-server-authentication-certificates',
  SecurityServerClients = 'security-server-clients',
  Forbidden = 'forbidden',
}

// A "single source of truth" for permission strings
export enum Permissions {
  INIT_CONFIG = 'INIT_CONFIG',
  SEARCH_MEMBERS = 'SEARCH_MEMBERS',
  VIEW_MEMBERS = 'VIEW_MEMBERS',
  ADD_NEW_MEMBER = 'ADD_NEW_MEMBER',
  VIEW_MEMBER_DETAILS = 'VIEW_MEMBER_DETAILS',
  EDIT_MEMBER_NAME_AND_ADMIN_CONTACT = 'EDIT_MEMBER_NAME_AND_ADMIN_CONTACT',
  ADD_SECURITY_SERVER_REG_REQUEST = 'ADD_SECURITY_SERVER_REG_REQUEST',
  MEMBER_TO_GLOBAL_GROUP_ADD_REMOVE = 'MEMBER_TO_GLOBAL_GROUP_ADD_REMOVE',
  ADD_MEMBER_SUBSYSTEM = 'ADD_MEMBER_SUBSYSTEM',
  REMOVE_MEMBER_SUBSYSTEM = 'REMOVE_MEMBER_SUBSYSTEM',
  ADD_SECURITY_SERVER_CLIENT_REG_REQUEST = 'ADD_SECURITY_SERVER_CLIENT_REG_REQUEST',
  STOP_OR_CONTINUE_SECURITY_SERVER_CLIENT_DATA_EXCHANGE = 'STOP_OR_CONTINUE_SECURITY_SERVER_CLIENT_DATA_EXCHANGE',
  REMOVE_MEMBER_FROM_SECURITY_SERVER_CLIENTS = 'REMOVE_MEMBER_FROM_SECURITY_SERVER_CLIENTS',
  DELETE_MEMBER = 'DELETE_MEMBER',
  VIEW_SECURITY_SERVERS = 'VIEW_SECURITY_SERVERS',
  VIEW_SECURITY_SERVER_DETAILS = 'VIEW_SECURITY_SERVER_DETAILS',
  EDIT_SECURITY_SERVER_ADDRESS = 'EDIT_SECURITY_SERVER_ADDRESS',
  EDIT_SECURITY_SERVER_SECURITY_CATEGORY = 'EDIT_SECURITY_SERVER_SECURITY_CATEGORY',
  ADD_SECURITY_SERVER_AUTH_CERT_REG_REQUEST = 'ADD_SECURITY_SERVER_AUTH_CERT_REG_REQUEST',
  DELETE_SECURITY_SERVER_AUTH_CERT = 'DELETE_SECURITY_SERVER_AUTH_CERT',
  DELETE_SECURITY_SERVER = 'DELETE_SECURITY_SERVER',
  VIEW_GLOBAL_GROUPS = 'VIEW_GLOBAL_GROUPS',
  ADD_GLOBAL_GROUP = 'ADD_GLOBAL_GROUP',
  VIEW_GROUP_DETAILS = 'VIEW_GROUP_DETAILS',
  EDIT_GROUP_DESCRIPTION = 'EDIT_GROUP_DESCRIPTION',
  ADD_AND_REMOVE_GROUP_MEMBERS = 'ADD_AND_REMOVE_GROUP_MEMBERS',
  DELETE_GROUP = 'DELETE_GROUP',
  VIEW_CENTRAL_SERVICES = 'VIEW_CENTRAL_SERVICES',
  VIEW_CENTRAL_SERVICE_DETAILS = 'VIEW_CENTRAL_SERVICE_DETAILS',
  ADD_CENTRAL_SERVICE = 'ADD_CENTRAL_SERVICE',
  EDIT_IMPLEMENTING_SERVICE = 'EDIT_IMPLEMENTING_SERVICE',
  REMOVE_CENTRAL_SERVICE = 'REMOVE_CENTRAL_SERVICE',
  VIEW_APPROVED_CAS = 'VIEW_APPROVED_CAS',
  VIEW_APPROVED_CA_DETAILS = 'VIEW_APPROVED_CA_DETAILS',
  ADD_APPROVED_CA = 'ADD_APPROVED_CA',
  EDIT_APPROVED_CA = 'EDIT_APPROVED_CA',
  DELETE_APPROVED_CA = 'DELETE_APPROVED_CA',
  VIEW_APPROVED_TSAS = 'VIEW_APPROVED_TSAS',
  VIEW_APPROVED_TSA_DETAILS = 'VIEW_APPROVED_TSA_DETAILS',
  ADD_APPROVED_TSA = 'ADD_APPROVED_TSA',
  EDIT_APPROVED_TSA = 'EDIT_APPROVED_TSA',
  DELETE_APPROVED_TSA = 'DELETE_APPROVED_TSA',
  VIEW_MANAGEMENT_REQUESTS = 'VIEW_MANAGEMENT_REQUESTS',
  VIEW_MANAGEMENT_REQUEST_DETAILS = 'VIEW_MANAGEMENT_REQUEST_DETAILS',
  CONFIRM_AUTH_CERT_REG_REQUEST = 'CONFIRM_AUTH_CERT_REG_REQUEST',
  DECLINE_AUTH_CERT_REG_REQUEST = 'DECLINE_AUTH_CERT_REG_REQUEST',
  CONFIRM_SECURITY_SERVER_CLIENT_REG_REQUEST = 'CONFIRM_SECURITY_SERVER_CLIENT_REG_REQUEST',
  DECLINE_SECURITY_SERVER_CLIENT_REG_REQUEST = 'DECLINE_SECURITY_SERVER_CLIENT_REG_REQUEST',
  VIEW_MEMBER_CLASSES = 'VIEW_MEMBER_CLASSES',
  ADD_MEMBER_CLASS = 'ADD_MEMBER_CLASS',
  EDIT_MEMBER_CLASS = 'EDIT_MEMBER_CLASS',
  DELETE_MEMBER_CLASS = 'DELETE_MEMBER_CLASS',
  VIEW_SECURITY_CATEGORIES = 'VIEW_SECURITY_CATEGORIES',
  ADD_SECURITY_CATEGORY = 'ADD_SECURITY_CATEGORY',
  EDIT_SECURITY_CATEGORY = 'EDIT_SECURITY_CATEGORY',
  DELETE_SECURITY_CATEGORY = 'DELETE_SECURITY_CATEGORY',
  VIEW_CONFIGURATION = 'VIEW_CONFIGURATION',
  EDIT_MANAGEMENT_SERVICES_ADDRESS = 'EDIT_MANAGEMENT_SERVICES_ADDRESS',
  EDIT_SIGNING_KEY_AND_CERT = 'EDIT_SIGNING_KEY_AND_CERT',
  VIEW_CONFIGURATION_MANAGEMENT = 'VIEW_CONFIGURATION_MANAGEMENT',
  VIEW_TRUSTED_ANCHORS = 'VIEW_TRUSTED_ANCHORS',
  UPLOAD_TRUSTED_ANCHOR = 'UPLOAD_TRUSTED_ANCHOR',
  DELETE_TRUSTED_ANCHOR = 'DELETE_TRUSTED_ANCHOR',
  DOWNLOAD_TRUSTED_ANCHOR = 'DOWNLOAD_TRUSTED_ANCHOR',
  VIEW_INTERNAL_CONFIGURATION_SOURCE = 'VIEW_INTERNAL_CONFIGURATION_SOURCE',
  VIEW_EXTERNAL_CONFIGURATION_SOURCE = 'VIEW_EXTERNAL_CONFIGURATION_SOURCE',
  GENERATE_SOURCE_ANCHOR = 'GENERATE_SOURCE_ANCHOR',
  DOWNLOAD_SOURCE_ANCHOR = 'DOWNLOAD_SOURCE_ANCHOR',
  UPLOAD_CONFIGURATION_PART = 'UPLOAD_CONFIGURATION_PART',
  DOWNLOAD_CONFIGURATION_PART = 'DOWNLOAD_CONFIGURATION_PART',
  ACTIVATE_TOKEN = 'ACTIVATE_TOKEN',
  DEACTIVATE_TOKEN = 'DEACTIVATE_TOKEN',
  GENERATE_SIGNING_KEY = 'GENERATE_SIGNING_KEY',
  ACTIVATE_SIGNING_KEY = 'ACTIVATE_SIGNING_KEY',
  DELETE_SIGNING_KEY = 'DELETE_SIGNING_KEY',
  VIEW_SYSTEM_SETTINGS = 'VIEW_SYSTEM_SETTINGS',
  REGISTER_SERVICE_PROVIDER = 'REGISTER_SERVICE_PROVIDER',
  BACKUP_CONFIGURATION = 'BACKUP_CONFIGURATION',
  RESTORE_CONFIGURATION = 'RESTORE_CONFIGURATION',
  VIEW_VERSION = 'VIEW_VERSION',
  CREATE_API_KEY = 'CREATE_API_KEY', // api key
  UPDATE_API_KEY = 'UPDATE_API_KEY', // api key
  REVOKE_API_KEY = 'REVOKE_API_KEY', // api key
  VIEW_API_KEYS = 'VIEW_API_KEYS', // api key
}

// A single source of truth for roles
export const Roles = [
  'XROAD_REGISTRATION_OFFICER',
  'XROAD_SECURITY_OFFICER',
  'XROAD_SYSTEM_ADMINISTRATOR',
];

export const mainTabs: Tab[] = [
  {
    to: { name: RouteName.Members },
    key: 'members',
    name: 'tab.main.members',
    permissions: [
      Permissions.VIEW_MEMBERS,
      Permissions.VIEW_MEMBER_DETAILS,
      Permissions.SEARCH_MEMBERS,
    ],
  },
  {
    to: { name: RouteName.SecurityServers },
    key: 'keys',
    name: 'tab.main.securityServers',
    permissions: [
      Permissions.VIEW_SECURITY_SERVERS,
      Permissions.VIEW_SECURITY_SERVER_DETAILS,
    ],
  },
  {
    to: { name: RouteName.ManagementRequests },
    key: 'managementRequests',
    name: 'tab.main.managementRequests',
    permissions: [
      Permissions.VIEW_MANAGEMENT_REQUESTS,
      Permissions.VIEW_MANAGEMENT_REQUEST_DETAILS,
    ],
  },
  {
    to: { name: RouteName.TrustServices },
    key: 'trustServices',
    name: 'tab.main.trustServices',
    permissions: [
      Permissions.VIEW_APPROVED_CAS,
      Permissions.VIEW_APPROVED_TSAS,
    ],
  },
  {
    to: { name: RouteName.InternalConfiguration },
    key: 'globalConfiguration',
    name: 'tab.main.globalConfiguration',
    permissions: [
      Permissions.VIEW_CONFIGURATION_MANAGEMENT,
      Permissions.VIEW_EXTERNAL_CONFIGURATION_SOURCE,
      Permissions.VIEW_INTERNAL_CONFIGURATION_SOURCE,
    ],
  },
  {
    to: { name: RouteName.GlobalResources }, // name of the firsts child of settings
    key: 'settings',
    name: 'tab.main.settings',
    permissions: [Permissions.VIEW_SYSTEM_SETTINGS],
  },
];

// Version 7.0 colors as enum.
export enum Colors {
  Purple10 = '#efebfb',
  Purple20 = '#e0d8f8',
  Purple30 = '#d1c4f4',
  Purple70 = '#9376e6',
  Purple100 = '#663cdc',
  Black10 = '#e8e8e8',
  Black30 = '#bcbbbb',
  Black50 = '#908e8e',
  Black70 = '#636161',
  Black100 = '#211e1e',
  White100 = '#ffffff',
  Yellow = '#f2994A',
  WarmGrey10 = '#f4f3f6',
  WarmGrey20 = '#eae8ee',
  WarmGrey30 = '#dedce4',
  WarmGrey50 = '#c9c6d3',
  WarmGrey70 = '#b4afc2',
  WarmGrey100 = '#575169',
  Error = '#ec4040',
  Success100 = '#0cc177',
  Success10 = '#e6f8f1',
  Background = '#e5e5e5',
}

export const Timeouts = {
  POLL_SESSION_TIMEOUT: 30000,
} as const;
