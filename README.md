# eCDP-Serial-Code
*Dont want to download anything? try using my web interface(tm): https://ecdp.cbps.xyz/.*

Generates "Serial Code" for the rarest DS Game ever; eCDP ! 

Windows and Linux Binaries: https://github.com/KuromeSan/eCDP-Serial-Code/releases/latest

# Other implementations:

-- Python

Turns out "LazyDogP" was reversing it around the same time i was, writing his own implementation in python, ulthough i beat him to it by 2 days, 
mine was completed on Nov 25, 2020 (sometime before midnight probably, on commit https://github.com/KuromeSan/eCDP-Serial-Code/commit/01a79303ac327262b3bf775bead79afe08821f19)

his was completed on  Nov 27, 2020, ulthough he lost the race he didnt know he was competing in but still a good reversing effort,
https://gist.github.com/lazydogP/57c431cc7fddb5b969559c4c8cd8c881

mine acturally worked like 50% of the time the day before that but im not counting that because it wasnt 100% success.

-- JavaScript 

One of the users on the eCDP Speedrunning discord called "User670" ported the algorithm to JavaScript, 
so i am now using his version for the Web Interface rather than running my C Code via php exec() like i was doing before 
its slightly faster, and more secure to do it this way too, so yeah.
