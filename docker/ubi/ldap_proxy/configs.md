# LDAP Proxy Configurations

This file documents all the GMS-specific configurations in the `ldap_proxy` Docker image.

## Packages Installed via YUM

Using YUM we install the following packages:

- [httpd](https://httpd.apache.org/docs/2.4/programs/httpd.html) is the Apache HyperText Transfer Protocol (HTTP) server program. It is designed to be run as a standalone daemon process. When used like this it will create a pool of child processes or threads to handle requests. **The following packages are all modules for the Apache HTTP Server.**
    - [mod_ldap](https://httpd.apache.org/docs/2.4/mod/mod_ldap.html) improves the performance of websites relying on backend connections to LDAP servers. In addition to the functions provided by the standard LDAP libraries, this module adds an LDAP connection pool and an LDAP shared memory cache.
    - [mod_authnz_ldap](https://httpd.apache.org/docs/2.4/mod/mod_authnz_ldap.html) allows authentication front-ends to authenticate users through an ldap directory.
    - [mod_ssl](http://httpd.apache.org/docs/current/mod/mod_ssl.html) provides SSL v3 and TLS v1.x support for the Apache HTTP Server.

## docker-entrypoint.sh

When you restart the container `httpd` can be confused by an incompletely-shutdown http context.  `httpd` won't start correctly if it thinks it is already running.  The [solution](https://github.com/CentOS/CentOS-Dockerfiles/blob/master/httpd/centos7/run-httpd.sh) is to remove any existing httpd pid files.

```
rm -rf /run/httpd/* /tmp/httpd*
```

## default-site.conf

Using the Apache HTTP Server modules `mod_ldap`, `mod_authnz_ldap`, and `mod_ssl` this configuration file defines our proxy through the following module-specific directives.

```
LDAPTrustedGlobalCert CA_BASE64 "/certs/ldap-ca.cert"
```
The ability to create an SSL and TLS connections to an LDAP server is defined by the directive [LDAPTrustedGlobalCert](https://httpd.apache.org/docs/2.4/mod/mod_ldap.html#ldaptrustedglobalcert).  It specifies the directory path and file name of the trusted CA certificates and/or system wide client certificates `mod_ldap` should use when establishing an SSL or TLS connection to an LDAP server. Here we have defined the location of our our trusted CA certificate and the type ` CA_BASE64`, which indicates we are using a PEM encoded CA certificate.

```
ErrorLog /dev/stderr
TransferLog /dev/stdout
```
The [ErrorLog](https://httpd.apache.org/docs/2.4/mod/core.html#errorlog) directive specifies where the server will log errors.

The [TransferLog](http://httpd.apache.org/docs/current/mod/mod_log_config.html#transferlog) directive that specifies where the server will log requests to the server.

```
ServerName 0.0.0.0

```
The [ServerName](https://httpd.apache.org/docs/2.4/mod/core.html#servername) directive sets request scheme, hostname and port that the server uses to identify itself. 

### VirtualHost

```
DocumentRoot "/www/proxy"
```
In deciding what file to serve for a given request, httpd's default behavior is to take the URL-Path for the request (the part of the URL following the hostname and port) and add it to the end of the [DocumentRoot](https://httpd.apache.org/docs/2.4/mod/core.html#documentroot) specified in your configuration files.


```

ProxyPass / ${PROXIED_URL}
ProxyPassReverse / ${PROXIED_URL}
ProxyRequests Off
ProxyPreserveHost On
```
The directives [ProxyPass](https://httpd.apache.org/docs/2.4/mod/mod_proxy.html#proxypass) and [ProxyPassReverse](https://httpd.apache.org/docs/2.4/mod/mod_proxy.html#proxypassreverse) are configured so that Apache HTTP Server will act as a reverse proxy.  
The [ProxyRequests](https://httpd.apache.org/docs/2.4/mod/mod_proxy.html#proxyrequests) prevents Apache httpd from functioning as a forward proxy server. When enabled, the [ProxyPreserveHost](https://httpd.apache.org/docs/2.4/mod/mod_proxy.html#proxypreservehost) directive will pass the Host: line from the incoming request to the proxied host, instead of the hostname specified in the ProxyPass line. For more information on configuration options you can view the offical documentation for [Apache Module mod_proxy](https://httpd.apache.org/docs/2.4/mod/mod_proxy.html)

```
AuthType Basic
AuthName "LDAP Protected"
AuthBasicProvider ldap
Require valid-user

```
The settings for the `AuthType`, `AuthName`, `AuthBasicProvider`, and `Require` directives are taken from the [example configuration](https://httpd.apache.org/docs/2.4/mod/mod_ldap.html#Example) in the `mod_ldap` docs.

```
AuthLDAPURL "ldap://${LDAP_HOST}:${LDAP_PORT}/${BASE_DN}?${ATTRIBUTE}?${SCOPE}?${FILTER}"
AuthLDAPBindDN "${BIND_DN}"
AuthLDAPBindPassword "${BIND_PASS}"
```
There are two phases in granting access to a user. The first phase is authentication, in which the `mod_authnz_ldap` authentication provider verifies that the user's credentials are valid. This is also called the search/bind phase. The second phase is authorization, in which `mod_authnz_ldap` determines if the authenticated user is allowed access to the resource in question. This is also known as the compare phase.

The following directives are used during the search/bind phase:
-`AuthLDAPURL`: Specifies the LDAP server, the base DN, the attribute to use in the search, as well as the extra search filter to use.
-`AuthLDAPBindDN`: An optional DN to bind with during the search phase.
-`AuthLDAPBindPassword`: An optional password to bind with during the search phase.

## CMD

```
CMD [ "/usr/sbin/apachectl", "-DFOREGROUND" ]
```

[According to Apache](https://httpd.apache.org/docs/2.4/programs/httpd.html), in general, `httpd` should not be invoked directly, but rather should be invoked via apachectl on Unix-based system. 
