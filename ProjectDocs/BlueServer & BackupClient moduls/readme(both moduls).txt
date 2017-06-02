====================================================================================================
[PROPERTIES]

backuper_modul_on=true  ----------> if this option is "on" the modul will work "interval" or "time" based,
if this property is set to "false" the modul can only perform "back_up_now"
 
interval_based_backup=false  ----------> if this option = false -> it means that the modul is then time based.
interval_in_howrs=3 ----------> interval between backups if  "interval_based_backup" = true
backup_time=12:06 ---------->  the format is "HH:MM"
backup_dir=c:/test ----------> the directory into which the backup is to be made
backup_dir_2=      ----------> leave this blank & the backup will be done only to "backup_dir", fill in if you want to make backup to 2 destinations
folders_to_copy=c:/projects;c:/tmp ----------> define folders to be copied separated with ";"
folders_to_skip=c:/projects/new;c:/projects/new_2 ----------> define folders not to be copied
max_file_size_mb=1000 -------------> define the max allowed size of a file
====================================================================================================
[IMPORTANT]

#2. 
#2.1 The file is backuped only if the source file is newer then the destination file.
#2.2 All properties can be changed while the program is running.
#2.3 OBS! To change properties while the program is running can be done by
copying the file -> changing the copied file -> and then replace the original file by pasting the copied one

====================================================================================================
[RESTARTER MODUL]

# The Buckuper modul needs "restarter.jar" because when the backuping is
finished it restarts the "mcautostarter" in order to unblock the files. 
The restart occurs after 1 min, because if it starts directly the Backuping modul 
will start backuping aggain as the time as still the same, if the backuper modul
runs after "time_based_backup"