require(reshape)
require(scales)
#
require(Cairo)
require(ggplot2)
require(RColorBrewer)
require(grid)
require(gridExtra)
require(lattice)
#
data=read.csv(file="../data/dutch_power_demand.txt",header=F,sep=",")
plot(data$V1,t="l")
#
df=data.frame(time=c(1:length(data$V1)),value=data$V1)
df_red<-df;df_red$value<-rep(NA,35040);df_red[11249:(11249+754),]<-df[11249:(11249+754),]
df_do<-df;df_do$value<-rep(NA,35040);df_do[7564:(7564+757),]<-df[7564:(7564+757),]
df_dg<-df;df_dg$value<-rep(NA,35040);df_dg[12085:(12085+756),]<-df[12085:(12085+756),]
p <- ggplot(df, aes(time, value)) + geom_line(color="blue", lwd=0.3) + theme_bw() +
  scale_x_continuous(breaks=seq(0,35000,5000)) +
  ggtitle("Dataset Dutch Power Demand and 3 anomalies discovered by SAXSequitur")
p  
p <- p + geom_line(aes(df_red$time,df_red$value),color="brown3",size=1) + theme_classic() +
  geom_line(aes(df_do$time,df_do$value),color="darkorchid1",size=1) +
  geom_line(aes(df_dg$time,df_dg$value),color="darkgreen",size=1) +
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank())
p
density=read.csv(file="../coverage.txt",header=F,sep=",")
density_df=data.frame(time=c(1:length(density$V1)),value=density$V1)
shade <- rbind(c(0,0), density_df, c(length(data$V1),0))
names(shade)<-c("x","y")
p3 <- ggplot(density_df, aes(x=time,y=value)) +
  geom_line(col="cyan2") + theme_classic() + scale_x_continuous(breaks=seq(0,35000,5000)) +
  geom_polygon(data = shade, aes(x, y), fill="cyan", alpha=0.5) +
  ggtitle("Sequitur rules density")+ 
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank())
p3
which(density[1:30000,]<190)

distances=read.csv(file="../distances.txt",header=F,sep=",")
df=data.frame(time=c(1:length(data$V1)),value=distances$V2,width=distances$V3)
p2 <- ggplot(df, aes(time, value)) + geom_line(color="red") + theme_classic() +
  scale_x_continuous(breaks=seq(0,35000,5000)) +
  ggtitle("Non-self distance to the nearest neighbor among subsequences corresponding to Sequitur rules") + 
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank())
p2 

print(arrangeGrob(p,p2,p3), ncol=1)

Cairo(width = 1000, height = 600, 
      file="DutchPD_new.pdf", 
      type="pdf", pointsize=12, 
      bg = "transparent", canvas = "white", units = "px", dpi = 82)
print(arrangeGrob(p,p3,p2), ncol=1)
dev.off()

distances=read.csv(file="../data/PAKDD/DutchPD/Dutch_power_demand_distances_curve.csv",header=F,sep=",")

#
#df_red<-df;df_red$value<-rep(NA,35040);df_red[11249:(11249+754),]<-df[11249:(11249+754),]
#df_do<-df;df_do$value<-rep(NA,35040);df_do[7564:(7564+757),]<-df[7564:(7564+757),]
#df_dg<-df;df_dg$value<-rep(NA,35040);df_dg[12085:(12085+756),]<-df[12085:(12085+756),]
#
df_null<-data.frame(x=c(1:757),y=rep(NA,757))
df_r<-df_null;df_r[1:755,2]<-df[11249:(11249+754),2]
df_o<-df_null;df_o[1:758,2]<-df[7564:(7564+757),2] 
df_g<-df_null;df_g[1:757,2]<-df[12085:(12085+756),2] 
plot(df[(96*110):(96*127),2],type="l")
p41 <- ggplot(df_r, aes(x, y)) + geom_line(col="brown3") +
  scale_x_continuous(breaks=c(seq(0,600,200),761)) + theme_bw() +
  theme(axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank(),
        panel.border=element_blank(),panel.background = element_blank(),
        panel.grid.major=element_blank(),panel.grid.minor = element_blank(),
        axis.line = element_line(colour = "black"),axis.line.y = element_blank()) +
  ggtitle("Best discord, ")
p41
p42 <- ggplot(df_o, aes(x, y)) + geom_line(col="darkorchid1") + theme_bw() +
  scale_x_continuous(breaks=c(seq(0,600,200),754)) + 
  theme(axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank(),
        panel.border=element_blank(),panel.background = element_blank(),
        panel.grid.major=element_blank(),panel.grid.minor = element_blank(),
        axis.line = element_line(colour = "black"),axis.line.y = element_blank()) +
  ggtitle("Second to best discord")
p42
p43 <- ggplot(df_g, aes(x, y)) + geom_line(col="darkgreen") + theme_bw() +
  scale_x_continuous(breaks=c(seq(0,600,200),757)) + 
  theme(axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank(),
        panel.border=element_blank(),panel.background = element_blank(),
        panel.grid.major=element_blank(),panel.grid.minor = element_blank(),
        axis.line = element_line(colour = "black"),axis.line.y = element_blank()) +
  ggtitle("Third discord")
p43

df_f<-df_null;df_f[1:721,2]<-df[480:1200,2]
p45 <- ggplot(df_f, aes(x, y)) + geom_line(col="blue") + theme_bw() +
  scale_x_continuous(breaks=c(seq(0,600,200),750)) +
  theme(axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank(),
        panel.border=element_blank(),panel.background = element_blank(),
        panel.grid.major=element_blank(),panel.grid.minor = element_blank(),
        axis.line = element_line(colour = "black"),axis.line.y = element_blank()) +
  ggtitle("Normal week")
p45


print(arrangeGrob(p45,p41,p42,p43), ncol=2)

Cairo(width = 900, height = 300, 
      file="DutchPD_new1.ps", 
      type="ps", pointsize=12, 
      bg = "transparent", canvas = "white", units = "px", dpi = 82)
print(arrangeGrob(p45,p41,p42,p43), ncol=2)
dev.off()

library(zoo)
zoo.tmp = zoo(density)
m.av<-rollmean(zoo.tmp, 1200,fill = list(NA, NULL, NA))
yy = as.numeric(unlist(coredata(m.av)))
p4 = ggplot(density_df, aes(x=time,y=value)) +
  geom_line(col="cyan2") + theme_bw() + 
  geom_polygon(data = shade, aes(x, y), fill="cyan", alpha=0.5) +
  ggtitle("Sequitur rules density")
p4 + geom_line(aes(x=density_df$time,y=yy),color="red")

ggplot(p29, aes(dt, ambtemp)) + geom_line() + 
  geom_line(aes(dt,amb.av),color="red") + 
  scale_x_datetime(breaks = date_breaks("5 min"),labels=date_format("%H:%M")) +
  xlab("Time 00.00 ~ 24:00 (2007-09-29)") + ylab("Tempreture")+
  ggtitle("Node 29")