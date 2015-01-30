#!#!/system/bin/sh

rm -r /mnt/sdcard/Lestraces   
mkdir /mnt/sdcard/Lestraces
rm -r /mnt/sdcard/PatternsSyscalls 
mkdir /mnt/sdcard/PatternsSyscalls 
while true; do

ps > /mnt/sdcard/ResPS.txt 


cat /mnt/sdcard/ResPS.txt | grep -v 'strace' | grep -o -e ' com.' > /mnt/sdcard/ActiveAPP.txt

cat /mnt/sdcard/ResPS.txt | grep -v 'strace' | grep ' com.' |ps | awk -F ' ' '{print $2}' | tail +2> /mnt/sdcard/ActivePID.txt 


cat /mnt/sdcard/ActivePID.txt | while read a; do strace -p $a -o "/mnt/sdcard/Lestraces/$a.csv" & echo $a ; done


sleep 20

cat /mnt/sdcard/ActivePID.txt | while read b; do grep -o -e ^'\([a-z]\|[A-Z]\)\+\(_\|[0-9]\|[a-z]\|[A-Z]\)*' "/mnt/sdcard/Lestraces/$b.csv"  > "/mnt/sdcard/PatternsSyscalls/$b.csv"  ; done

done


