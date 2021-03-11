jsonFileLongBlink = 'LongBlink.json';
jsonFileShortBlink = 'ShortBlink.json';

jsonDataLongBlink = jsondecode(fileread(jsonFileLongBlink));
jsonDataShortBlink = jsondecode(fileread(jsonFileShortBlink));

dataLongBlink = jsonDataLongBlink.data;
dataShortBlink = jsonDataShortBlink.data;

chanelOneLongBlink=dataLongBlink(:,2);
chanelOneShortBlink=dataShortBlink(:,2);

originalSignalLongBlink = chanelOneLongBlink;
originalSignalShortBlink = chanelOneShortBlink;

numSamplesPerSecond= 256;
sampleLength = 1020;
fc = 3;
fs = numSamplesPerSecond; 
orderOfTheFilter = 5; 
[b,a] = butter(orderOfTheFilter,fc/(fs/2));

absoluteSignalLongBlink=abs(originalSignalLongBlink - mean(originalSignalLongBlink));
absoluteSignalShortBlink=abs(originalSignalShortBlink - mean(originalSignalShortBlink));

filteredSignalLongBlink = filter(b,a,absoluteSignalLongBlink);
filteredSignalShortBlink = filter(b,a,absoluteSignalShortBlink);

firstSampleStart= (5*sampleLength)-sampleLength;
firstSampleEnd= 5*sampleLength;

secondSampleStart= (10*sampleLength)-sampleLength;
secondSampleEnd= 10*sampleLength;

firstSampleLongBlink = filteredSignalLongBlink(firstSampleStart:firstSampleEnd)
firstSampleLongBlink =(firstSampleLongBlink - min(firstSampleLongBlink ))/(max(firstSampleLongBlink )-min(firstSampleLongBlink ))
secondSampleLongBlink = filteredSignalLongBlink(secondSampleStart:secondSampleEnd)
secondSampleLongBlink =(secondSampleLongBlink - min(secondSampleLongBlink ))/(max(secondSampleLongBlink )-min(secondSampleLongBlink ))

firstSampleShortBlink = filteredSignalShortBlink(firstSampleStart:firstSampleEnd)
firstSampleShortBlink =(firstSampleShortBlink - min(firstSampleShortBlink ))/(max(firstSampleShortBlink )-min(firstSampleShortBlink ))
secondSampleShortBlink = filteredSignalShortBlink(secondSampleStart:secondSampleEnd)
secondSampleShortBlink =(secondSampleShortBlink - min(secondSampleShortBlink ))/(max(secondSampleShortBlink )-min(secondSampleShortBlink ))


d1=dtw(secondSampleLongBlink,firstSampleLongBlink)
d2=dtw(secondSampleLongBlink,firstSampleShortBlink)
d3=dtw(secondSampleShortBlink, firstSampleLongBlink )
d4=dtw(secondSampleShortBlink, firstSampleShortBlink )

subplot(4,1,1);
plot(firstSampleLongBlink)
subplot(4,1,2);
plot(secondSampleLongBlink)
subplot(4,1,3);
plot(firstSampleShortBlink)
subplot(4,1,4);
plot(secondSampleShortBlink)


