sudo /etc/init.d/denyhosts stop

sudo perl -i.bak -ne "print unless \$_ =~ /$1/" /etc/hosts.deny

for f in $(grep -l "$1" /var/lib/denyhosts/*); do
  echo "removing $1 from $f"
  sudo perl -i.bak -ne "print unless \$_ =~ /^$1/" $f
done

sudo /etc/init.d/denyhosts start

