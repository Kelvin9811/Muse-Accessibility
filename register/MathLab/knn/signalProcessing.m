function processedSignal = signalProcessing(originalSignal)


[b,a] = coefsFilter();


%filteredSignal= filter(b,a,originalSignal);

%processedSignal = filteredSignal;
processedSignal = originalSignal;
end