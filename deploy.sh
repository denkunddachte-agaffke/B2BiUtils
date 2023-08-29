#!/bin/bash

cd $(dirname $(readlink -f $0))

./gradlew $GRADLE_OPTS clean distTar
if [ $? != 0 ]; then
  exit $?
fi

pkg=$(ls -1t B2BUtils/build/distributions/*.tar.gz|head -1)
if [ ! -f "$pkg" ]; then
  echo "Not package found!"
  exit 1
fi

USERNAME=${USERNAME:-$(id -un)}

fn=$(basename $pkg)
pkgdir=${fn%.tar.gz}
h=sla31144.srv.allianz
echo "Deploy $fn to $h..."
scp -p $pkg $USERNAME@$h:/tmp
ssh $USERNAME@$h "d=/opt/cd/sfg/.users/$USERNAME; cd \$d; rm -rf $pkgdir; mkdir $pkgdir; tar xfz /tmp/$fn -C \$d/$pkgdir --strip 1; cd \$HOME; rm b2biutils; ln -s \$d/$pkgdir b2biutils; rm /tmp/$fn"

h=sla80908.srv.allianz
echo "Deploy $fn to $h..."
scp -p $pkg $USERNAME@$h:/tmp
ssh $USERNAME@$h "d=/opt/SFG/users/$USERNAME; cd \$d; rm -rf $pkgdir; mkdir $pkgdir; tar xfz /tmp/$fn -C \$d/$pkgdir --strip 1; cd \$HOME; rm b2biutils; ln -s \$d/$pkgdir b2biutils; rm /tmp/$fn"

h=slb00040.fltrnsfr-we1.we1.azure.aztec.cloud.allianz
echo "Deploy $fn to $h..."
scp -p $pkg $USERNAME@$h:/tmp
ssh $USERNAME@$h "d=/home/$USERNAME; cd \$d; rm -rf $pkgdir; mkdir $pkgdir; tar xfz /tmp/$fn -C \$d/$pkgdir --strip 1; cd \$HOME; rm b2biutils; ln -s \$d/$pkgdir b2biutils; rm /tmp/$fn"

  