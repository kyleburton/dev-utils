exec < /dev/tty
pn=`rn_pair_name`
read -p "Type enter to use pair name '$pn', or enter correct pair name: " user_input
if [ "$user_input" = "" ]; then
  exit 0
fi
set-pair $user_input
