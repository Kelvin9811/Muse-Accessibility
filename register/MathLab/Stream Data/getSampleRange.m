function sample = getSampleRange(signal,sampleEnd)

sampleLength = 765;

sampleStart= sampleEnd-sampleLength;

if sampleStart<1
    sampleStart = 1;
end

if sampleEnd>51000
    sampleEnd = 51000;
end

sample = signal(sampleStart:sampleEnd);

end