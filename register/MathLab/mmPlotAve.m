function mmPlotAve(museData)
hold on;
plot(museData.TimeStamp, (museData.Delta_TP9+museData.Delta_AF7+museData.Delta_AF8+museData.Delta_TP10)/4,'Color','#CC0000');
plot(museData.TimeStamp, (museData.Theta_TP9+museData.Theta_AF7+museData.Theta_AF8+museData.Theta_TP10)/4, 'Color','#9933CC');
plot(museData.TimeStamp, (museData.Alpha_TP9+museData.Alpha_AF7+museData.Alpha_AF8+museData.Alpha_TP10)/4, 'Color','#0099CC');
plot(museData.TimeStamp, (museData.Beta_TP9+museData.Beta_AF7+museData.Beta_AF8+museData.Beta_TP10)/4, 'Color','#669900');
plot(museData.TimeStamp, (museData.Gamma_TP9+museData.Gamma_AF7+museData.Gamma_AF8+museData.Gamma_TP10)/4, 'Color','#FF8A00');
title('Mind Monitor - Absolute Brain Waves');
hold off;
end