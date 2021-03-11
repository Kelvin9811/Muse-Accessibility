
LongBlink='LongBlink.json';
%personOne='C:\Users\kortiz\Documents\MATLAB\Random Sample\Dataset\1\LongBlink.json';
%personTwo='C:\Users\kortiz\Documents\MATLAB\Random Sample\Dataset\2\LongBlink.json';
%personThree='C:\Users\kortiz\Documents\MATLAB\Random Sample\Dataset\3\LongBlink.json';
%personFour='C:\Users\kortiz\Documents\MATLAB\Random Sample\Dataset\4\LongBlink.json';
%personFive='C:\Users\kortiz\Documents\MATLAB\Random Sample\Dataset\5\LongBlink.json';
%personSix='C:\Users\kortiz\Documents\MATLAB\Random Sample\Dataset\6\LongBlink.json';
%personSeven='C:\Users\kortiz\Documents\MATLAB\Random Sample\Dataset\7\LongBlink.json';

sampleLength = 1020;


jsonData = jsondecode(fileread(LongBlink));
data = jsonData.data;
chanelOne=data(:,5);
hold on
rand = randi([1 50])
sampleStart= (rand*sampleLength)-sampleLength;
sampleEnd= rand*sampleLength;
plot(chanelOne(sampleStart:sampleEnd))
hold off



%{


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
%}