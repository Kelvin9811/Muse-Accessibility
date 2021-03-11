% TSNE vamos ausar para pasar una matriz de V[50][1020]
% a un vector de V[50][2]

jsonFileShortBlink = 'ShortBlink.json';
jsonDataShortBlink  = jsondecode(fileread(jsonFileShortBlink));

jsonFileLongBlink = 'LongBlink.json';
jsonDataLongBlink  = jsondecode(fileread(jsonFileLongBlink));

%Columna 1 = Timestamp, Columna 2,3,4 y 5 = Canal 1,2,3 y 4

osShortBlink=jsonDataShortBlink.data(:,4);
osLongBlink=jsonDataLongBlink.data(:,4);

plot(osShortBlink(1530:2040));