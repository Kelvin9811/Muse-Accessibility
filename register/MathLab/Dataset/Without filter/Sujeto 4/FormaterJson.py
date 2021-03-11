jsonFile = "LongBlink.json"
csvFile = "largos.json"

name = "Kelvin Ortiz"
age = "22"
height = "1.74"
weight = "65"
rested = "true"
gender = "male"
hair = "short"
blinkType = "short"

Datajson = open(jsonFile, "w")
Datajson.write(
    "{\"name\": \""+name +
    "\",\"age\": \""+age +
    "\",\"height\": \""+height +
    "\",\"weight\": \""+weight +
    "\",\"rested\": "+rested +
    ",\"blinkType\": \""+blinkType +
    "\",\"data\": ")

Datacsv = open(csvFile, "r")
line = Datacsv .readline()
while line:
    Datajson.write(""+line+"],")
    line = Datacsv .readline()

Datajson.write("]}")
Datajson.close()
Datacsv.close()
