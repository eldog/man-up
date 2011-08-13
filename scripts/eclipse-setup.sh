#!/bin/bash
set -o errexit
set -o nounset

# Eclipse
E_URL='http://www.mirrorservice.org/sites/download.eclipse.org/eclipseMirror'
E_URL="${E_URL}/technology/epp/downloads/release/indigo/R"
E_URL="${E_URL}/eclipse-java-indigo-linux-gtk-x86_64.tar.gz"

E_DIR='eclipse'

cd /tmp/
wget -O - "${E_URL}" \
    | sudo tar --directory=/opt/ \
               --no-same-owner \
               --owner root \
               --group root \
               -xz

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

