
personOne='C:\Users\kortiz\Documents\MATLAB\Random Sample\Dataset\1\ShortBlink.json';
personTwo='C:\Users\kortiz\Documents\MATLAB\Random Sample\Dataset\2\ShortBlink.json';
personThree='C:\Users\kortiz\Documents\MATLAB\Random Sample\Dataset\3\ShortBlink.json';
personFour='C:\Users\kortiz\Documents\MATLAB\Random Sample\Dataset\4\ShortBlink.json';
personFive='C:\Users\kortiz\Documents\MATLAB\Random Sample\Dataset\5\ShortBlink.json';
personSix='C:\Users\kortiz\Documents\MATLAB\Random Sample\Dataset\6\ShortBlink.json';
personSeven='C:\Users\kortiz\Documents\MATLAB\Random Sample\Dataset\7\ShortBlink.json';
sampleLength = 1020;


jsonData = jsondecode(fileread(personOne));
data = jsonData.data;
chanelOne=data(:,2);
hold on
rand = randi([1 50])
sampleStart= (rand*sampleLength)-sampleLength;
sampleEnd= rand*sampleLength;
plot(chanelOne(sampleStart:sampleEnd))
hold off


jsonData = jsondecode(fileread(personTwo));
data = jsonData.data;
chanelOne=data(:,2);
hold on
rand = randi([1 50])
sampleStart= (rand*sampleLength)-sampleLength;
sampleEnd= rand*sampleLength;
plot(chanelOne(sampleStart:sampleEnd))
hold off


jsonData = jsondecode(fileread(personThree));
data = jsonData.data;
chanelOne=data(:,2);
hold on
rand = randi([1 50])
sampleStart= (rand*sampleLength)-sampleLength;
sampleEnd= rand*sampleLength;
plot(chanelOne(sampleStart:sampleEnd))
hold off


jsonData = jsondecode(fileread(personFour));
data = jsonData.data;
chanelOne=data(:,2);
hold on
rand = randi([1 50])
sampleStart= (rand*sampleLength)-sampleLength;
sampleEnd= rand*sampleLength;
plot(chanelOne(sampleStart:sampleEnd))
hold off