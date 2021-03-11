function sample = getSample(signal,sampleNumber)

sampleLength = 1020;

sampleStart= (sampleNumber*sampleLength)-sampleLength;

if sampleStart<1
    sampleStart = 1;
end

sampleEnd= sampleNumber*sampleLength;
if sampleEnd>51000
    sampleEnd = 51000;
end
sample = signal(sampleStart:sampleEnd);

end