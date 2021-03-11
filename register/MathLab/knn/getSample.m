function sample = getSample(signal,sampleNumber)

sampleLength = 510;

trainingSampleStart= (sampleNumber*sampleLength)-sampleLength;

if trainingSampleStart<1
    trainingSampleStart = 1;
end

trainingSampleEnd= sampleNumber*sampleLength;
if trainingSampleEnd>7650
    trainingSampleEnd = 7650;
end
sample = signal(trainingSampleStart:trainingSampleEnd);

end