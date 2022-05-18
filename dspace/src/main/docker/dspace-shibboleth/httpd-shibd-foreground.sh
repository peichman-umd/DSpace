#!/bin/bash
#
# The contents of this file are subject to the license and copyright
# detailed in the LICENSE and NOTICE files at the root of the source
# tree and available online at
#
# http://www.dspace.org/license/
#

# This script simply starts up the Shibboleth Daemon & Apache in the foreground
set -e

# Replace the ${APACHE_SERVER_NAME} placeholder in our shibboleth2.xml with the current value of the
# $APACHE_SERVER_NAME environment variable.
sed -i "s/\${APACHE_SERVER_NAME}/$APACHE_SERVER_NAME/g" /etc/shibboleth/shibboleth2.xml
sed -i "s;\${SP_ENTITY_ID};$SP_ENTITY_ID;g" /etc/shibboleth/shibboleth2.xml
sed -i "s/\${HANDLER_SSL}/$HANDLER_SSL/g" /etc/shibboleth/shibboleth2.xml
sed -i "s/\${HANDLER_SECURE_STR}/$HANDLER_SECURE_STR/g" /etc/shibboleth/shibboleth2.xml
sed -i "s/\${IDP_ENTITY_ID}/$IDP_ENTITY_ID/g" /etc/shibboleth/shibboleth2.xml
sed -i "s;\${IDP_METADATA_URL};$IDP_METADATA_URL;g" /etc/shibboleth/shibboleth2.xml

# Start the Shibboleth daemon
/etc/init.d/shibd start

# Remove existing Apache PID files (if any)
rm -f /var/run/apache2/apache2.pid

# Start Apache (in foreground)
exec /usr/sbin/apache2ctl -DFOREGROUND
