require("R.matlab")
alarms<-readMat("/home/psenin/Téléchargements/chan_1min.mat")
signals<-readMat("/home/psenin/Téléchargements/alm_sig_1min.mat")
str(signals)
str(alarms)
str(signals)
asys=cbind(alarms[[2]],signals[[2]])

dim((asys[asys[,1]==1,])[1,])
dim(asys)

par(mfrow=c(5,2),mar=c(0.2,0.2,0.2,0.2))
for(i in c(40:49)){
  plot((asys[asys[,1]==1,])[i,2:7501],type="l",yaxt='n',xaxt='n', ann=FALSE)
}

par(mfrow=c(5,2),mar=c(0.2,0.2,0.2,0.2))
for(i in c(20:29)){
  plot((asys[asys[,1]==3,])[i,2:7501],type="l",yaxt='n',xaxt='n', ann=FALSE)
}

#example1
dat=(asys[asys[,1]==1,])[45,2:7501]
plot(dat,type="l")
write.table(dat,file="../data/brady45.txt",col.names=F,row.names=F)
