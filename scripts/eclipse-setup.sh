#!/bin/bash
set -o errexit
set -o nounset

# Eclipse
E_URL='http://www.mirrorservice.org/sites/download.eclipse.org/eclipseMirror'
E_URL="${E_URL}/technology/epp/downloads/release/indigo/R"
E_URL="${E_URL}/eclipse-java-indigo-linux-gtk-x86_64.tar.gz"

wget --no-clobber -O /tmp/eclipse.tar.gz "${E_URL}"
sudo tar --directory=/opt/ -xzf /tmp/eclipse.tar.gz
sudo chown -R `whoami`:`groups | cut -d ' ' -f 1` /opt/eclipse

sudo tee /usr/bin/eclipse <<EOF
#!/bin/sh
export ECLIPSE_HOME='/opt/eclipse'

\${ECLIPSE_HOME}/eclipse "\$@"
EOF
sudo chmod 755 /usr/bin/eclipse

sudo tee /usr/share/applications/eclipse.desktop <<EOF
[Desktop Entry]
Encoding=UTF-8
Name=Eclipse
Comment=Eclipse IDE
Exec=eclipse
Icon=/opt/eclipse/icon.xpm
Terminal=false
Type=Application
Categories=GNOME;Application;Development;
StartupNotify=true
EOF

