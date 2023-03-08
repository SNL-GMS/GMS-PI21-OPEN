ARG SOURCE_IMAGE
ARG SOURCE_TAG
FROM ${SOURCE_IMAGE}:${SOURCE_TAG}

ARG UBI_RPM_URL
ARG ALMALINUX_RPM_URL
ARG EPEL_YUM_URL

# locale
ENV LANG="en_US.UTF-8"
ENV LANGUAGE="en_US.UTF-8"
ENV LC_ALL="en_US.UTF-8"

COPY src/*.repo /etc/yum.repos.d/

# Reset shell to source environment files and always use bash.
SHELL [ "/bin/bash", "-lc" ]

USER 0

RUN set -ex && \
    # remove the ironbank repo and replace with a gms version
    rm -f /etc/yum.repos.d/ironbank.repo && \
    # setup repos
    sed -i -e "s~#UBI_RPM_URL#~$UBI_RPM_URL~" /etc/yum.repos.d/ubi.repo && \
    sed -i -e "s~#ALMALINUX_RPM_URL#~$ALMALINUX_RPM_URL~" /etc/yum.repos.d/almalinux.repo && \
    sed -i -e "s~#EPEL_YUM_URL#~$EPEL_YUM_URL~" /etc/yum.repos.d/epel.repo && \
    # configure yum
    echo "skip_missing_names_on_install=0" >> /etc/yum.conf && \
    # disable docs
    echo "tsflags=nodocs" >> /etc/yum.conf && \
    echo "tsflags=nodocs" >> /etc/dnf.conf && \
    # install a few common things
    dnf install -y gettext \
                   glibc-locale-source \
                   glibc-langpack-en \
                   jq \
                   less \
                   wget && \
    # set locale
    localedef -i en_US -f UTF-8 en_US.UTF-8 && \
    echo "LANG=\"en_US.UTF-8\"" > /etc/locale.conf && \
    # clean up
    dnf clean all && \
    rm -rf /var/cache/dnf && \
    # add gms user for use in other images
    groupadd --gid 1001 gms && \
    useradd --gid 1001 --uid 1001 -d /opt/gms gms && \
    chmod 755 /opt/gms

# gms uid
USER 1001

CMD ["/bin/bash"]
