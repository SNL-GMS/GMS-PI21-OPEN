# LDAP Proxy

This image acts as a proxy for Kibana that uses LDAP as a middlewear to control user access. To get more info on GMS-specific configuration see the [configuration documentation](configs.md).

## Getting Started

### Prerequisities

In order to run this container you'll need docker installed.

- [Windows](https://docs.docker.com/windows/started)
- [OS X](https://docs.docker.com/mac/started/)
- [Linux](https://docs.docker.com/linux/started/)

This service expects to be able to reach a Kibana instance at `http://kibana:5601`

This service only authenticates with LDAP over TLS, any other configurations will not work.

### Building

To build this image you must ensure that the LDAP cacert file you are using is:
* base64 encoded
* named `ldap-ca.cert`
* located in the `opt/src` directory

### Usage

Relevant documentation:

* [Apache mod_ldap](!https://httpd.apache.org/docs/2.4/mod/mod_ldap.html)
* [Apache mod_authnz_ldap](!https://httpd.apache.org/docs/2.4/mod/mod_authnz_ldap.html)
* [Upgrade doc because most examples are old](!http://httpd.apache.org/docs/2.4/upgrading.html)

#### Environment Variables

* `LDAP_HOST:` - Speciefies the LDAP host
* `LDAP_PORT:` - Speciefies the LDAP port
* `BASE_DN` - Specifies the base DN to use in the search 
* `ATTRIBUTE:` - The attribute to compare login with (e.g. uid)
* `SCOPE:` - The scope of the search (e.g. sub)
* `FILTER` - A valid LDAP search filter
* `BIND_DN` - Specifies the DN to use during a search
* `BIND_PASS` - Specifies the password to bind with during a search
* `PROXIED_URL` - Specifies the URL that is being proxied

#### Useful File Locations

- `src/docker-entrypoint.sh` - this entrypoint removes some configuration files to ensure the container behaves as expected
- `opt/load_ldap.sh` - this script loads the LDAP certificate used to connect with TLS to the LDAP server 
- `opt/src/ldap-ca.cert` - this is the certificate that the load_certs.sh file will use to connect to the LDAP server over TLS with

### Testing

```bash
cd test
dgoss run -e PROXIED_URL="http://test.com" \
          -e BIND_DN="test" \
          -e BIND_PASS="test" \
          -e LDAP_HOST="test.com" \
          -e LDAP_PORT="389" \
          -e BASE_DN="" \
          -e ATTRIBUTE="" \
          -e SCOPE="" \
          -e FILTER="" \
          ${DOCKER_REGISTRY}/${REGISTRY_BASE}/centos7/ldap_proxy:${TAG}
```