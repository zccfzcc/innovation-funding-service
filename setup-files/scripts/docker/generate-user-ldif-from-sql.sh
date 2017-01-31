#!/bin/bash

# A Munro: 30 Jan 2017
# Generate ldif from a sql file, so we get the uids right.
# So uid's in ifs db needs to match ldap uids.
#
# sql file format:
# UPDATE `user` SET `uid`='8dd6f464-ad69-499f-a1fd-d8a42529866c' WHERE `email`='nancy.james@example.com';
# Can ignore all the ldif fields generated by openldap...
#
# ldif format (from slapcat; without all the modify timestamp stuff):
#
# dn: uid=13a55bb5-8b14-4487-92d7-7165ce378c88,dc=nodomain
# uid: 13a55bb5-8b14-4487-92d7-7165ce378c88
# mail: steve.smith@empire.com
# sn:: IA==
# cn:: IA==
# objectClass: inetOrgPerson
# objectClass: person
# objectClass: top
# userPassword:: e1NTSEF9b2lRZUF1OHNrR0VqQmhweUpmV01hOFF3M0dNK2xRd2Q=
# structuralObjectClass: inetOrgPerson
# employeeType: active

domain="dc=nodomain"

# The infamous user password: Passw0rd
password="e1NTSEF9b2lRZUF1OHNrR0VqQmhweUpmV01hOFF3M0dNK2xRd2Q="

[ $# -ne 1 ] && {
  echo "Was expecting 1 arg: <sql-file>."
  exit 1
}

IFS=$'\n'
for l in $(cat $1)
do
  unset IFS
  l=$( echo $l|sed -e "s/'//g" -e 's/`//g' -e 's/;//g')
  f=($l)

  # Pick out the fields
  for i in ${f[*]}
  do
    [[ $i =~ ^email ]] && email=$(echo $i|cut -d= -f2)
    [[ $i =~ ^uid ]] && uid=$(echo $i|cut -d= -f2)
  done

  [[ $email =~ ifs_system_maintenance_user ]] && continue

  echo "dn: uid=$uid,$domain"
  echo "uid: $uid"
  echo "mail: $email"
  echo "sn:: IA=="
  echo "cn:: IA=="
  echo "objectClass: inetOrgPerson"
  echo "objectClass: person"
  echo "objectClass: top"
  echo "userPassword:: $password"
  echo "structuralObjectClass: inetOrgPerson"
  echo "employeeType: active"
  echo ""
done
