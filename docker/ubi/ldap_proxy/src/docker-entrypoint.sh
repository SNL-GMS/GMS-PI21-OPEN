#!/bin/sh
set -e

# Check to see if the variable LDAP_USE_TLS exists and if so,
# determine it's value.  If the value is set to zero, delete
# the line in the conf file correcsponding to the LDAP cert
if ! [ -z "${LDAP_USE_TLS}" ]; then
   # Variable is defined so get its value
   if [ "$LDAP_USE_TLS" -eq 0 ]; then
      # Insecure LDAP so remove the LDAP Cert from the config file
      sed -i '/LDAPTrustedGlobalCert/d' /etc/httpd/conf.d/default-site.conf
   fi
fi

exec "$@"
