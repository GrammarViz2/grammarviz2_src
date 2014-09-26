require(akima)
require(rgl)

# ECG 0606
#
dat = read.table("ecg0606__a5.txt",sep=",",header=T,as.is=T)
#
xyz <- with(dat, interp(x=grammarSize,y=approximationDistance,z=maxZeroRunLength, 
                        xo=sort(unique(grammarSize)), duplicate="strip", extrap=FALSE ))
with(xyz, persp3d(x,y,z, col=heat.colors(length(z))[rank(z)], xlab='grammarSize', 
                  ylab='approximationDistance', zlab='maxZeroRunLength', main=''))

# Grammar size 500
#
slice=dat[dat$grammarSize>480 & dat$grammarSize<520,]
slice[slice$totalZeroes>0,] # this parameters set 20-6-3, while yields zero-coverage regions is wrong

# Grammar size 1000
#
slice=dat[dat$grammarSize>900 & dat$grammarSize<1100,]
plot3d(slice)
slice[with(slice, order(totalZeroes)), ]
dat[dat$totalZeroes==1,]

xyz <- with(slice, interp(x=grammarSize,y=approximationDistance,z=totalZeroes, 
                        xo=sort(unique(grammarSize)), duplicate="mean", extrap=FALSE ))
with(slice, persp3d(x,y,z, col=heat.colors(length(z))[rank(z)], xlab='grammarSize', 
                  ylab='approximationDistance', zlab='totalZeroes', main=''))

# Grammar size 1300
#
slice=dat[dat$grammarSize>1200 & dat$grammarSize<1400,]
plot3d(slice)
slice[with(slice, order(totalZeroes)), ]
dat[dat$totalZeroes==1,]

xyz <- with(slice, interp(x=grammarSize,y=approximationDistance,z=totalZeroes, 
                          xo=sort(unique(grammarSize)), duplicate="mean", extrap=FALSE ))
with(slice, persp3d(x,y,z, col=heat.colors(length(z))[rank(z)], xlab='grammarSize', 
                    ylab='approximationDistance', zlab='totalZeroes', main=''))