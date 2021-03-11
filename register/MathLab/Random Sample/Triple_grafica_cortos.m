jsonFile = 'ShortBlink.json';
jsonData = jsondecode(fileread(jsonFile));
data = jsonData.data;
chanelOne=data(:,2);

originalSignal = chanelOne;

numSamplesPerSecond= 256;
sampleLength = 1020;
fc = 5;
fs = numSamplesPerSecond; 
orderOfTheFilter = 5; 
[b,a] = butter(orderOfTheFilter,fc/(fs/2));

absoluteSignal=(originalSignal-mean(originalSignal));

filteredSignal = filter(b,a,absoluteSignal);

rand = randi([1 50]);

sampleStart= (rand*sampleLength)-sampleLength;
sampleEnd= rand*sampleLength;

p = bandpower(filteredSignal(sampleStart:sampleEnd));

subplot(3,1,1);
plot(originalSignal(sampleStart:sampleEnd))
subplot(3,1,2);
plot(absoluteSignal(sampleStart:sampleEnd))
subplot(3,1,3);
plot(filteredSignal(sampleStart:sampleEnd))

