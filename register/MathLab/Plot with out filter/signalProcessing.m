function processedSignal = signalProcessing(originalSignal)


[b,a] = coefsFilter();


processedSignal= filter(b,a,originalSignal);


end