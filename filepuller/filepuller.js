var rp = require('request-promise');
var fs = require('fs');
var htmlToText = require('html-to-text');

var urlInfo = [
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%AE%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A7_(%E0%A4%B8%E0%A5%83%E0%A4%B7%E0%A5%8D%E0%A4%9F%E0%A4%BF%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%AE%E0%A5%8D)/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "padmapurANa-sRShTi-khaNDa-",
    //     numFiles: 82,
    //     numDigits: 2 
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%AE%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A8_(%E0%A4%AD%E0%A5%82%E0%A4%AE%E0%A4%BF%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "padmapurANa-bhUmi-khaNDa-",
    //     numFiles: 125,
    //     numDigits: 3        
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%AE%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A9_(%E0%A4%B8%E0%A5%8D%E0%A4%B5%E0%A4%B0%E0%A5%8D%E0%A4%97%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "padmapurANa-svarga-khaNDa-",
    //     numFiles: 62,
    //     numDigits: 2        
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%AE%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%AA_(%E0%A4%AC%E0%A5%8D%E0%A4%B0%E0%A4%B9%E0%A5%8D%E0%A4%AE%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "padmapurANa-brahma-khaNDa-",
    //     numFiles: 26,
    //     numDigits: 2        
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%AE%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%AB_(%E0%A4%AA%E0%A4%BE%E0%A4%A4%E0%A4%BE%E0%A4%B2%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "padmapurANa-pAtALa-khaNDa-",
    //     numFiles: 117,
    //     numDigits: 3       
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%AE%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%AC_(%E0%A4%89%E0%A4%A4%E0%A5%8D%E0%A4%A4%E0%A4%B0%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "padmapurANa-uttara-khaNDa-",
    //     numFiles: 255,
    //     numDigits: 3       
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%AE%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%AD_(%E0%A4%95%E0%A5%8D%E0%A4%B0%E0%A4%BF%E0%A4%AF%E0%A4%BE%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "padmapurANa-kriyA-khaNDa-",
    //     numFiles: 26,
    //     numDigits: 2       
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A7_(%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A5%87%E0%A4%B6%E0%A5%8D%E0%A4%B5%E0%A4%B0%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%95%E0%A5%87%E0%A4%A6%E0%A4%BE%E0%A4%B0%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "skandapurANa-mAhEshvara-kEdAra-khaNDa-",
    //     numFiles: 35,
    //     numDigits: 2       
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A7_(%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A5%87%E0%A4%B6%E0%A5%8D%E0%A4%B5%E0%A4%B0%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%95%E0%A5%8C%E0%A4%AE%E0%A4%BE%E0%A4%B0%E0%A4%BF%E0%A4%95%E0%A4%BE%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "skandapurANa-mAhEshvara-kaumArikA-khaNDa-",
    //     numFiles: 66,
    //     numDigits: 2       
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A7_(%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A5%87%E0%A4%B6%E0%A5%8D%E0%A4%B5%E0%A4%B0%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%85%E0%A4%B0%E0%A5%81%E0%A4%A3%E0%A4%BE%E0%A4%9A%E0%A4%B2%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D_%E0%A5%A7/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "skandapurANa-mAhEshvara-khaNDa-aruNAchala1-",
    //     numFiles: 13,
    //     numDigits: 2       
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A7_(%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A5%87%E0%A4%B6%E0%A5%8D%E0%A4%B5%E0%A4%B0%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%85%E0%A4%B0%E0%A5%81%E0%A4%A3%E0%A4%BE%E0%A4%9A%E0%A4%B2%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D_%E0%A5%A8/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "skandapurANa-mAhEshvara-khaNDa-aruNAchala2-",
    //     numFiles: 24,
    //     numDigits: 2       
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A8_(%E0%A4%B5%E0%A5%88%E0%A4%B7%E0%A5%8D%E0%A4%A3%E0%A4%B5%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%B5%E0%A5%87%E0%A4%99%E0%A5%8D%E0%A4%95%E0%A4%9F%E0%A4%BE%E0%A4%9A%E0%A4%B2%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "skandapurANa-vaiShNava-khaNDa-vEMkaTAchala1-",
    //     numFiles: 40,
    //     numDigits: 2       
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A8_(%E0%A4%B5%E0%A5%88%E0%A4%B7%E0%A5%8D%E0%A4%A3%E0%A4%B5%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A5%81%E0%A4%B7%E0%A5%8B%E0%A4%A4%E0%A5%8D%E0%A4%A4%E0%A4%AE%E0%A4%9C%E0%A4%97%E0%A4%A8%E0%A5%8D%E0%A4%A8%E0%A4%BE%E0%A4%A5%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "skandapurANa-vaiShNava-khaNDa-puruShOttama-jagannAtha-",
    //     numFiles: 49,
    //     numDigits: 2       
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A8_(%E0%A4%B5%E0%A5%88%E0%A4%B7%E0%A5%8D%E0%A4%A3%E0%A4%B5%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%AC%E0%A4%A6%E0%A4%B0%E0%A4%BF%E0%A4%95%E0%A4%BE%E0%A4%B6%E0%A5%8D%E0%A4%B0%E0%A4%AE%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "skandapurANa-vaiShNava-khaNDa-badarikAshrama-",
    //     numFiles: 8,
    //     numDigits: 2       
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A8_(%E0%A4%B5%E0%A5%88%E0%A4%B7%E0%A5%8D%E0%A4%A3%E0%A4%B5%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%95%E0%A4%BE%E0%A4%B0%E0%A5%8D%E0%A4%A4%E0%A4%BF%E0%A4%95%E0%A4%AE%E0%A4%BE%E0%A4%B8%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "skandapurANa-vaiShNava-khaNDa-kArtIka-mAhAtmya-",
    //     numFiles: 36,
    //     numDigits: 2       
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A8_(%E0%A4%B5%E0%A5%88%E0%A4%B7%E0%A5%8D%E0%A4%A3%E0%A4%B5%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%AE%E0%A4%BE%E0%A4%B0%E0%A5%8D%E0%A4%97%E0%A4%B6%E0%A5%80%E0%A4%B0%E0%A5%8D%E0%A4%B7%E0%A4%AE%E0%A4%BE%E0%A4%B8%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "skandapurANa-vaiShNava-khaNDa-mArgashIrSha-mAhAtmya-",
    //     numFiles: 17,
    //     numDigits: 2       
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A8_(%E0%A4%B5%E0%A5%88%E0%A4%B7%E0%A5%8D%E0%A4%A3%E0%A4%B5%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%AD%E0%A4%BE%E0%A4%97%E0%A4%B5%E0%A4%A4%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "skandapurANa-vaiShNava-khaNDa-bhAgavata-mAhAtmya-",
    //     numFiles: 4,
    //     numDigits: 2       
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A8_(%E0%A4%B5%E0%A5%88%E0%A4%B7%E0%A5%8D%E0%A4%A3%E0%A4%B5%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%B5%E0%A5%88%E0%A4%B6%E0%A4%BE%E0%A4%96%E0%A4%AE%E0%A4%BE%E0%A4%B8%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "skandapurANa-vaiShNava-khaNDa-vaishAkha-mAhAtmya-",
    //     numFiles: 25,
    //     numDigits: 2       
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A8_(%E0%A4%B5%E0%A5%88%E0%A4%B7%E0%A5%8D%E0%A4%A3%E0%A4%B5%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%85%E0%A4%AF%E0%A5%8B%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "skandapurANa-vaiShNava-khaNDa-ayOdhyA-mAhAtmya-",
    //     numFiles: 10,
    //     numDigits: 2       
    // },
    // {
    //     urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A8_(%E0%A4%B5%E0%A5%88%E0%A4%B7%E0%A5%8D%E0%A4%A3%E0%A4%B5%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%B5%E0%A4%BE%E0%A4%B8%E0%A5%81%E0%A4%A6%E0%A5%87%E0%A4%B5%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
    //     fileNamePrefix: "skandapurANa-vaiShNava-khaNDa-vAsudEva-mAhAtmya-",
    //     numFiles: 32,
    //     numDigits: 2       
    // },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A9_(%E0%A4%AC%E0%A5%8D%E0%A4%B0%E0%A4%B9%E0%A5%8D%E0%A4%AE%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%B8%E0%A5%87%E0%A4%A4%E0%A5%81%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "skandapurANa-brahma-khaNDa-sEtu-",
        numFiles: 52,
        numDigits: 2       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A9_(%E0%A4%AC%E0%A5%8D%E0%A4%B0%E0%A4%B9%E0%A5%8D%E0%A4%AE%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%A7%E0%A4%B0%E0%A5%8D%E0%A4%AE%E0%A4%BE%E0%A4%B0%E0%A4%A3%E0%A5%8D%E0%A4%AF_%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "skandapurANa-brahma-khaNDa-dharmAraNya-",
        numFiles: 40,
        numDigits: 2       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A9_(%E0%A4%AC%E0%A5%8D%E0%A4%B0%E0%A4%B9%E0%A5%8D%E0%A4%AE%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%AC%E0%A5%8D%E0%A4%B0%E0%A4%B9%E0%A5%8D%E0%A4%AE%E0%A5%8B%E0%A4%A4%E0%A5%8D%E0%A4%A4%E0%A4%B0_%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "skandapurANa-brahma-khaNDa-uttara-",
        numFiles: 22,
        numDigits: 2       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%AA_(%E0%A4%95%E0%A4%BE%E0%A4%B6%E0%A5%80%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "skandapurANa-kAshI-khaNDa-",
        numFiles: 100,
        numDigits: 3       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%AB_(%E0%A4%85%E0%A4%B5%E0%A4%A8%E0%A5%8D%E0%A4%A4%E0%A5%80%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%85%E0%A4%B5%E0%A4%A8%E0%A5%8D%E0%A4%A4%E0%A5%80%E0%A4%95%E0%A5%8D%E0%A4%B7%E0%A5%87%E0%A4%A4%E0%A5%8D%E0%A4%B0%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "skandapurANa-avantI-khaNDa-avantI-mAhAtmya-",
        numFiles: 71,
        numDigits: 2       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%AB_(%E0%A4%85%E0%A4%B5%E0%A4%A8%E0%A5%8D%E0%A4%A4%E0%A5%80%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%85%E0%A4%B5%E0%A4%A8%E0%A5%8D%E0%A4%A4%E0%A5%80%E0%A4%B8%E0%A5%8D%E0%A4%A5%E0%A4%9A%E0%A4%A4%E0%A5%81%E0%A4%B0%E0%A4%B6%E0%A5%80%E0%A4%A4%E0%A4%BF%E0%A4%B2%E0%A4%BF%E0%A4%99%E0%A5%8D%E0%A4%97%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "skandapurANa-avantI-khaNDa-84liMga-mAhAtmya-",
        numFiles: 84,
        numDigits: 2       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%AB_(%E0%A4%85%E0%A4%B5%E0%A4%A8%E0%A5%8D%E0%A4%A4%E0%A5%80%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%B0%E0%A5%87%E0%A4%B5%E0%A4%BE_%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "skandapurANa-avantI-khaNDa-rEvA-khaNDa-",
        numFiles: 132,
        numDigits: 3       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%AC_(%E0%A4%A8%E0%A4%BE%E0%A4%97%E0%A4%B0%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "skandapurANa-nAgara-khaNDa-",
        numFiles: 279,
        numDigits: 3       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%AD_(%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%AD%E0%A4%BE%E0%A4%B8%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%AD%E0%A4%BE%E0%A4%B8%E0%A4%95%E0%A5%8D%E0%A4%B7%E0%A5%87%E0%A4%A4%E0%A5%8D%E0%A4%B0_%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "skandapurANa-prabhAsa-khaNDa-prabhAsa-mAhAtmya-",
        numFiles: 365,
        numDigits: 3       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%AD_(%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%AD%E0%A4%BE%E0%A4%B8%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%B5%E0%A4%B8%E0%A5%8D%E0%A4%A4%E0%A5%8D%E0%A4%B0%E0%A4%BE%E0%A4%AA%E0%A4%A5%E0%A4%95%E0%A5%8D%E0%A4%B7%E0%A5%87%E0%A4%A4%E0%A5%8D%E0%A4%B0%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "skandapurANa-prabhAsa-khaNDa-vastrApatha-mAhAtmya-",
        numFiles: 19,
        numDigits: 2       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%AD_(%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%AD%E0%A4%BE%E0%A4%B8%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/%E0%A4%A6%E0%A5%8D%E0%A4%B5%E0%A4%BE%E0%A4%B0%E0%A4%95%E0%A4%BE%E0%A4%AE%E0%A4%BE%E0%A4%B9%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "skandapurANa-prabhAsa-khaNDa-dvArakA-mAhAtmya-",
        numFiles: 44,
        numDigits: 2       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%AE_(%E0%A4%85%E0%A4%AE%E0%A5%8D%E0%A4%AC%E0%A4%BF%E0%A4%95%E0%A4%BE%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/",
        fileNamePrefix: "skandapurANa-prabhAsa-khaNDa-ambikA-khaNDa-",
        numFiles: 19,
        numDigits: 2       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B8%E0%A5%8D%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%AE_(%E0%A4%85%E0%A4%AE%E0%A5%8D%E0%A4%AC%E0%A4%BF%E0%A4%95%E0%A4%BE%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83)/",
        fileNamePrefix: "skandapurANa-prabhAsa-khaNDa-ambikA-khaNDa-",
        numFiles: 19,
        numDigits: 2       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%A8%E0%A4%BE%E0%A4%B0%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D-_%E0%A4%AA%E0%A5%82%E0%A4%B0%E0%A5%8D%E0%A4%B5%E0%A4%BE%E0%A4%B0%E0%A5%8D%E0%A4%A7%E0%A4%83/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "nAradapurANa-01-",
        numFiles: 125,
        numDigits: 1       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%A8%E0%A4%BE%E0%A4%B0%E0%A4%A6%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D-_%E0%A4%89%E0%A4%A4%E0%A5%8D%E0%A4%A4%E0%A4%B0%E0%A4%BE%E0%A4%B0%E0%A5%8D%E0%A4%A7%E0%A4%83/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "nAradapurANa-02-",
        numFiles: 82,
        numDigits: 1       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%AF%E0%A5%81%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%AA%E0%A5%82%E0%A4%B0%E0%A5%8D%E0%A4%B5%E0%A4%BE%E0%A4%B0%E0%A5%8D%E0%A4%A7%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "vAyupurANa-01-",
        numFiles: 61,
        numDigits: 1       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%AF%E0%A5%81%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D/%E0%A4%89%E0%A4%A4%E0%A5%8D%E0%A4%A4%E0%A4%B0%E0%A4%BE%E0%A4%B0%E0%A5%8D%E0%A4%A7%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "vAyupurANa-02-",
        numFiles: 50,
        numDigits: 1       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%AD%E0%A4%B5%E0%A4%BF%E0%A4%B7%E0%A5%8D%E0%A4%AF%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D_/%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5_%E0%A5%A7_(%E0%A4%AC%E0%A5%8D%E0%A4%B0%E0%A4%BE%E0%A4%B9%E0%A5%8D%E0%A4%AE%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5)/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "bhaviShyapurANa-parva-01-",
        numFiles: 216,
        numDigits: 3       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%AD%E0%A4%B5%E0%A4%BF%E0%A4%B7%E0%A5%8D%E0%A4%AF%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D_/%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5_%E0%A5%A8_(%E0%A4%AE%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5)/%E0%A4%AD%E0%A4%BE%E0%A4%97%E0%A4%83_%E0%A5%A7/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "bhaviShyapurANa-parva-02-bhAga-01-",
        numFiles: 21,
        numDigits: 2       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%AD%E0%A4%B5%E0%A4%BF%E0%A4%B7%E0%A5%8D%E0%A4%AF%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D_/%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5_%E0%A5%A8_(%E0%A4%AE%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5)/%E0%A4%AD%E0%A4%BE%E0%A4%97%E0%A4%83_%E0%A5%A8/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "bhaviShyapurANa-parva-02-bhAga-02-",
        numFiles: 20,
        numDigits: 2       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%AD%E0%A4%B5%E0%A4%BF%E0%A4%B7%E0%A5%8D%E0%A4%AF%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D_/%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5_%E0%A5%A8_(%E0%A4%AE%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5)/%E0%A4%AD%E0%A4%BE%E0%A4%97%E0%A4%83_%E0%A5%A9/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "bhaviShyapurANa-parva-02-bhAga-03-",
        numFiles: 20,
        numDigits: 2       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%AD%E0%A4%B5%E0%A4%BF%E0%A4%B7%E0%A5%8D%E0%A4%AF%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D_/%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5_%E0%A5%A9_(%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%A4%E0%A4%BF%E0%A4%B8%E0%A4%B0%E0%A5%8D%E0%A4%97%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5)/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A7/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "bhaviShyapurANa-parva-03-bhAga-01-",
        numFiles: 7,
        numDigits: 2       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%AD%E0%A4%B5%E0%A4%BF%E0%A4%B7%E0%A5%8D%E0%A4%AF%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D_/%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5_%E0%A5%A9_(%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%A4%E0%A4%BF%E0%A4%B8%E0%A4%B0%E0%A5%8D%E0%A4%97%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5)/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A8/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "bhaviShyapurANa-parva-03-bhAga-02-",
        numFiles: 35,
        numDigits: 2       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%AD%E0%A4%B5%E0%A4%BF%E0%A4%B7%E0%A5%8D%E0%A4%AF%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D_/%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5_%E0%A5%A9_(%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%A4%E0%A4%BF%E0%A4%B8%E0%A4%B0%E0%A5%8D%E0%A4%97%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5)/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%A9/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "bhaviShyapurANa-parva-03-bhAga-03-",
        numFiles: 32,
        numDigits: 2       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%AD%E0%A4%B5%E0%A4%BF%E0%A4%B7%E0%A5%8D%E0%A4%AF%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D_/%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5_%E0%A5%A9_(%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%A4%E0%A4%BF%E0%A4%B8%E0%A4%B0%E0%A5%8D%E0%A4%97%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5)/%E0%A4%96%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%83_%E0%A5%AA/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "bhaviShyapurANa-parva-03-bhAga-04-",
        numFiles: 26,
        numDigits: 2       
    },
    {
        urlPrefix: "https://sa.wikisource.org/wiki/%E0%A4%AD%E0%A4%B5%E0%A4%BF%E0%A4%B7%E0%A5%8D%E0%A4%AF%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AE%E0%A5%8D_/%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5_%E0%A5%AA_(%E0%A4%89%E0%A4%A4%E0%A5%8D%E0%A4%A4%E0%A4%B0%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%B5)/%E0%A4%85%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%AF%E0%A4%83_",
        fileNamePrefix: "bhaviShyapurANa-parva-04-",
        numFiles: 208,
        numDigits: 3       
    },
    {
        urlList: [
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AE%E0%A5%81%E0%A4%96%E0%A4%AC%E0%A4%A8%E0%A5%8D%E0%A4%A7%E0%A4%A8%E0%A4%82",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%85",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%85%E0%A4%9A",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%85%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%B2%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%85%E0%A4%A8%E0%A4%BF%E0%A4%AF%E0%A4%AE%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%85%E0%A4%A8%E0%A5%8D%E0%A4%AF%E0%A4%A6%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%85%E0%A4%AD%E0%A4%BF%E0%A4%AF%E0%A5%8B%E0%A4%97%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%85%E0%A4%AE%E0%A5%8D%E0%A4%B2%E0%A4%B0%E0%A5%81%E0%A4%B9%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%85%E0%A4%B0%E0%A5%8D%E0%A4%A6%E0%A4%A8%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%85%E0%A4%B5%E0%A4%A7%E0%A4%BF%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%85%E0%A4%B6%E0%A5%8D%E0%A4%AE%E0%A4%9C%E0%A4%82",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%86",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%86%E0%A4%A8%E0%A5%81%E0%A4%AE%E0%A4%BE%E0%A4%A8%E0%A4%BF%E0%A4%95%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%86%E0%A4%B5%E0%A4%B0%E0%A5%8D%E0%A4%A4%E0%A5%8D%E0%A4%A4%E0%A4%A8%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%87",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%89",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%89%E0%A4%A6%E0%A4%B0%E0%A5%8D%E0%A4%A6%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%89%E0%A4%AA%E0%A4%A8%E0%A4%AF%E0%A4%A8%E0%A4%82",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%8A",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%8B",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%8F",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%95",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%95%E0%A4%A6%E0%A4%AE%E0%A5%8D%E0%A4%AC%E0%A4%95%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%95%E0%A4%B0%E0%A5%87%E0%A4%A3%E0%A5%81%E0%A4%B8%E0%A5%81%E0%A4%A4%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%95%E0%A4%B0%E0%A5%8D%E0%A4%B5%E0%A5%8D%E0%A4%B5%E0%A5%81%E0%A4%B0%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%95%E0%A4%BE%E0%A4%95%E0%A4%AA%E0%A4%95%E0%A5%8D%E0%A4%B7%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%95%E0%A4%BE%E0%A4%B0%E0%A4%95%E0%A5%81%E0%A4%95%E0%A5%8D%E0%A4%B7%E0%A5%80%E0%A4%AF%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%95%E0%A4%BE%E0%A4%B6%E0%A4%BF%E0%A4%95%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%95%E0%A5%81%E0%A4%9A%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%95%E0%A5%81%E0%A4%B2%E0%A4%BE%E0%A4%AF%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%95%E0%A5%83%E0%A4%95%E0%A4%B2%E0%A4%BE%E0%A4%B6%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%95%E0%A5%87%E0%A4%B0%E0%A4%B2%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%95%E0%A5%8D%E0%A4%B0%E0%A4%AE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%95%E0%A5%8D%E0%A4%B7%E0%A5%81%E0%A4%B0%E0%A4%BF%E0%A4%95%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%96",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%97",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%97%E0%A4%A8%E0%A5%8D%E0%A4%A7%E0%A4%9C%E0%A4%BE%E0%A4%A4%E0%A4%82",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%97%E0%A4%BE%E0%A4%99%E0%A5%8D%E0%A4%97%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%97%E0%A5%81%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%95%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%97%E0%A5%8B%E0%A4%A1%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%97%E0%A5%8C%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%98",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%9A",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%9A%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A4%95%E0%A4%82",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%9A%E0%A4%BE%E0%A4%B0%E0%A4%82",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%9B",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%9C",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%9C%E0%A4%A8%E0%A4%BF%E0%A4%A4%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%9C%E0%A4%B2%E0%A4%BE%E0%A4%B6%E0%A4%AF%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%9C%E0%A5%87%E0%A4%A4%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%9D",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A4",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A4%E0%A4%B0%E0%A4%BE%E0%A4%A8%E0%A5%8D%E0%A4%A7%E0%A5%81%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A4%E0%A4%BE%E0%A4%B5%E0%A5%80%E0%A4%B7%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A4%E0%A5%81%E0%A4%9C",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A4%E0%A5%88%E0%A4%B2%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A6",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A6%E0%A4%B6%E0%A4%AC%E0%A4%B2%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A6%E0%A4%BF%E0%A4%A4%E0%A4%BF%E0%A4%B8%E0%A5%81%E0%A4%A4%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A6%E0%A5%81%E0%A4%B0%E0%A5%8D%E0%A4%97%E0%A4%BE%E0%A4%A7%E0%A5%8D%E0%A4%AF%E0%A4%95%E0%A5%8D%E0%A4%B7%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A6%E0%A5%87%E0%A4%B6%E0%A5%8D%E0%A4%AF%E0%A4%82",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A7",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A7%E0%A4%BE%E0%A4%B5%E0%A4%95%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A8",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A8%E0%A4%B0%E0%A4%95%E0%A4%9C%E0%A4%BF%E0%A4%A4%E0%A5%8D",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A8%E0%A4%BE%E0%A4%97%E0%A4%B5%E0%A4%BE%E0%A4%B0%E0%A4%BF%E0%A4%95%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A8%E0%A4%BE%E0%A4%B2%E0%A5%80%E0%A4%95%E0%A4%BF%E0%A4%A8%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A8%E0%A4%BF%E0%A4%B0%E0%A4%B6%E0%A4%A8%E0%A4%82",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A8%E0%A4%BF%E0%A4%B7%E0%A5%8D%E0%A4%95%E0%A5%8D%E0%A4%B0%E0%A4%AF%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%A8%E0%A5%87%E0%A4%AE%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AA",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AA%E0%A4%A4%E0%A4%A8%E0%A4%82",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%AE%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AA%E0%A4%B0%E0%A4%BF%E0%A4%AD%E0%A4%BE%E0%A4%B7%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AA%E0%A4%B5%E0%A4%82",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AA%E0%A4%BE%E0%A4%A3%E0%A4%BF%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AA%E0%A4%BE%E0%A4%B0%E0%A4%A6%E0%A4%BE%E0%A4%B0%E0%A4%BF%E0%A4%95%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AA%E0%A4%BF%E0%A4%A1%E0%A4%95%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AA%E0%A5%80%E0%A4%A4%E0%A4%95%E0%A4%A6%E0%A4%B2%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A4%A4%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AA%E0%A5%81%E0%A4%B0%E0%A5%81%E0%A4%B7%E0%A4%A4%E0%A5%8D%E0%A4%B5%E0%A4%82",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AA%E0%A5%82%E0%A4%9C%E0%A4%BF%E0%A4%A4%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AA%E0%A5%8B%E0%A4%9F%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%A4%E0%A4%BE%E0%A4%AA%E0%A4%A8%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%A6%E0%A4%95%E0%A5%8D%E0%A4%B7%E0%A4%BF%E0%A4%A3%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%BE%E0%A4%A5%E0%A4%AE%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%BF%E0%A4%95%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AB",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AC",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AC%E0%A4%B2%E0%A4%BF%E0%A4%B7%E0%A5%8D%E0%A4%A3%E0%A5%81%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AC%E0%A4%BE%E0%A4%B2%E0%A5%8D%E0%A4%AF%E0%A4%82",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AC%E0%A5%8D%E0%A4%B0%E0%A4%B9%E0%A5%8D%E0%A4%AE%E0%A4%A6%E0%A4%A4%E0%A5%8D%E0%A4%A4%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AD",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AD%E0%A4%AA%E0%A4%A4%E0%A4%BF%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AD%E0%A4%BE%E0%A4%B0%E0%A4%B5%E0%A4%BF%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AD%E0%A5%82%E0%A4%95%E0%A4%B2%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AD%E0%A5%87%E0%A4%95%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AE%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%B2%E0%A4%82",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AE%E0%A4%A7%E0%A5%81%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AE%E0%A4%A8%E0%A5%8D%E0%A4%A4%E0%A4%B5%E0%A5%8D%E0%A4%AF%E0%A4%82",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AE%E0%A4%AF%E0%A5%82%E0%A4%B0%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AE%E0%A4%B9%E0%A4%BE%E0%A4%95%E0%A5%81%E0%A4%B2%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AE%E0%A4%B9%E0%A4%BE%E0%A4%B7%E0%A5%8D%E0%A4%9F%E0%A4%AE%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AE%E0%A4%BE%E0%A4%A4%E0%A5%83%E0%A4%B5%E0%A4%BE%E0%A4%B9%E0%A4%BF%E0%A4%A8%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AE%E0%A4%BE%E0%A4%B2%E0%A5%81%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AE%E0%A5%81%E0%A4%97%E0%A5%82%E0%A4%B9%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AE%E0%A5%82%E0%A4%B0%E0%A5%8D%E0%A4%A6%E0%A5%8D%E0%A4%A7%E0%A4%95%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AF",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AF%E0%A4%B5%E0%A4%95%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%AF%E0%A5%81%E0%A4%9C",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B0",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B0%E0%A4%A4%E0%A4%BF%E0%A4%AA%E0%A4%A4%E0%A4%BF%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B0%E0%A4%B8%E0%A4%BE%E0%A4%B2%E0%A4%B8%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B0%E0%A4%BE%E0%A4%9C%E0%A4%B8%E0%A4%AD%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B0%E0%A4%BE%E0%A4%AE%E0%A4%95%E0%A4%B0%E0%A5%8D%E0%A4%AA%E0%A5%82%E0%A4%B0%E0%A4%95%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B0%E0%A5%81%E0%A4%B9",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B2",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B2%E0%A4%BE%E0%A4%9C",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5%E0%A4%A4%E0%A5%8D%E0%A4%B8%E0%A4%B2%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5%E0%A4%B0%E0%A5%82%E0%A4%A5%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5%E0%A4%B2%E0%A4%AF%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5%E0%A4%BE%E0%A4%97%E0%A5%80%E0%A4%B6%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5%E0%A4%BE%E0%A4%AF%E0%A4%95%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5%E0%A4%BE%E0%A4%B8%E0%A4%BF%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5%E0%A4%BF%E0%A4%97%E0%A4%BE%E0%A4%A8%E0%A4%82",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5%E0%A4%BF%E0%A4%A7",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5%E0%A4%BF%E0%A4%AE%E0%A4%B2%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5%E0%A4%BF%E0%A4%B6%E0%A4%AF%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5%E0%A4%BF%E0%A4%B7%E0%A4%BE%E0%A4%AF%E0%A5%81%E0%A4%A7%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5%E0%A5%80%E0%A4%B0",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5%E0%A5%83%E0%A4%B9%E0%A4%A4%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5%E0%A5%88%E0%A4%9C%E0%A4%AF%E0%A4%A8%E0%A5%8D%E0%A4%A4%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5%E0%A5%8B%E0%A4%9F%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B5%E0%A5%8D%E0%A4%AF%E0%A4%BE%E0%A4%A1%E0%A5%8D%E0%A4%B0%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B6",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B6%E0%A4%B5",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B6%E0%A4%BE%E0%A4%B2",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B6%E0%A4%BF%E0%A4%B2%E0%A5%8C%E0%A4%95%E0%A4%BE%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B6%E0%A4%BF%E0%A4%B5%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%BF%E0%A4%AF%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B6%E0%A5%81%E0%A4%95%E0%A4%A8%E0%A4%BE%E0%A4%AE%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B6%E0%A5%87%E0%A4%AB%E0%A4%BE%E0%A4%B2%E0%A4%BF%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B6%E0%A5%8D%E0%A4%B0%E0%A4%A5",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B6%E0%A5%8D%E0%A4%B0%E0%A5%81",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B7",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B8",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B8%E0%A4%99%E0%A5%8D%E0%A4%97%E0%A4%B5%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B8%E0%A4%A8%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%A3%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B8%E0%A4%AA%E0%A5%8D%E0%A4%A4%E0%A4%A8%E0%A4%BE%E0%A4%AE%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B8%E0%A4%AE%E0%A5%82%E0%A4%B9%E0%A4%A8%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B8%E0%A4%B0%E0%A5%8D%E0%A4%AA%E0%A4%B8%E0%A4%A4%E0%A5%8D%E0%A4%B0%E0%A4%82",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B8%E0%A4%BE%E0%A4%97%E0%A4%B0%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B8%E0%A4%BE%E0%A4%B2%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B8%E0%A5%81%E0%A4%95%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B8%E0%A5%81%E0%A4%B0%E0%A4%BE%E0%A4%9C%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B8%E0%A5%82%E0%A4%B0%E0%A5%8D%E0%A4%AF%E0%A5%8D%E0%A4%AF%E0%A4%B2%E0%A4%A4%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B8%E0%A5%8C%E0%A4%A7%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B8%E0%A5%8D%E0%A4%A5%E0%A4%B2%E0%A4%95%E0%A5%81%E0%A4%AE%E0%A5%81%E0%A4%A6%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B8%E0%A5%8D%E0%A4%AF%E0%A4%AE",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B8%E0%A5%8D%E0%A4%B5%E0%A4%B0%E0%A5%8D%E0%A4%97%E0%A4%B2%E0%A5%8B%E0%A4%95%E0%A5%87%E0%A4%B6%E0%A4%83",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B9",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B9%E0%A4%B0%E0%A4%BF%E0%A4%B2%E0%A5%87",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83/%E0%A4%B9%E0%A4%BF",
        "https://sa.wikisource.org/wiki/%E0%A4%B6%E0%A4%AC%E0%A5%8D%E0%A4%A6%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A5%81%E0%A4%AE%E0%A4%83"
        ],
        fileNamePrefix: "shabdakalpadruma-",
        numFiles: -1,
        numDigits: 1
    },
    {
        urlList: [
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%99%E0%A5%8D%E0%A4%97%E0%A4%B0%E0%A4%BE%E0%A4%97",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A6%E0%A4%BE%E0%A4%AF",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A8%E0%A4%A8%E0%A5%8D%E0%A4%A4%E0%A4%B6%E0%A5%80%E0%A4%B0%E0%A5%8D%E0%A4%B7%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A8%E0%A5%81%E0%A4%B2%E0%A5%87%E0%A4%AA",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%A8%E0%A5%8D%E0%A4%AF%E0%A4%B2%E0%A4%BF%E0%A4%99%E0%A5%8D%E0%A4%97",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%AA%E0%A4%BF",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%AD%E0%A4%BE%E0%A4%97",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%AD%E0%A5%8D%E0%A4%B0%E0%A4%BF",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%B0%E0%A4%A3%E0%A5%8D%E0%A4%AF%E0%A4%9C%E0%A5%80%E0%A4%B5",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%B2%E0%A4%98%E0%A5%81",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%B5%E0%A4%A7%E0%A5%80%E0%A4%B0",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%B5%E0%A4%BF%E0%A4%A4%E0%A5%8D%E0%A4%A4",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%B6%E0%A5%8D%E0%A4%AE%E0%A4%95%E0%A5%87%E0%A4%A4%E0%A5%81",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%B7%E0%A5%8D%E0%A4%9F%E0%A4%BE%E0%A4%B0",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%85%E0%A4%B8%E0%A5%81%E0%A4%B0%E0%A4%B0%E0%A4%BE%E0%A4%9C%E0%A5%8D",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%86",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%86%E0%A4%98%E0%A4%BE%E0%A4%B0",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%86%E0%A4%A4%E0%A5%83%E0%A4%A3%E0%A5%8D%E0%A4%A3",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%86%E0%A4%A6%E0%A4%BF%E0%A4%A6%E0%A5%87%E0%A4%B5",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%86%E0%A4%AE%E0%A5%8B%E0%A4%A6",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%86%E0%A4%B0%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%86%E0%A4%B6%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%86%E0%A4%B8%E0%A4%99%E0%A5%8D%E0%A4%97",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%87",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%87%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A4%95",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%87%E0%A4%AD%E0%A5%8D%E0%A4%AF%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%88",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%89",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%89%E0%A4%A4%E0%A5%8D%E0%A4%A4%E0%A4%AE%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%89%E0%A4%A4%E0%A5%8D%E0%A4%AA%E0%A4%BE%E0%A4%A6%E0%A4%BF%E0%A4%A4",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%89%E0%A4%A6%E0%A4%BE%E0%A4%B0",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%89%E0%A4%AA%E0%A4%98%E0%A4%BE%E0%A4%A4",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%89%E0%A4%AA%E0%A4%AE%E0%A4%A8%E0%A5%8D%E0%A4%A5%E0%A4%A8%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%89%E0%A4%AA%E0%A4%AF%E0%A4%BE%E0%A4%AE",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%89%E0%A4%AA%E0%A4%B2%E0%A4%95%E0%A5%8D%E0%A4%B7%E0%A4%A3",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%89%E0%A4%AA%E0%A4%BE%E0%A4%AF%E0%A4%A8",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%8A",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%8B",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%8B%E0%A4%A4%E0%A4%BE%E0%A4%A8%E0%A4%BF",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A5%A0",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%8F",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%8F%E0%A4%95%E0%A4%BE%E0%A4%A6%E0%A4%BF",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%90",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%93",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%94",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A4%A6%E0%A4%A8",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A4%AE%E0%A4%B2%E0%A4%95%E0%A5%80%E0%A4%9F",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A4%B0%E0%A5%8D%E0%A4%A4%E0%A5%8D%E0%A4%A4%E0%A4%B0%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A4%B0%E0%A5%8D%E0%A4%AE%E0%A5%8D%E0%A4%AE%E0%A4%B0%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A4%B2%E0%A4%AD",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A4%B2%E0%A5%8D%E0%A4%AA%E0%A4%95",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A4%BE%E0%A4%97",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A4%BE%E0%A4%AE%E0%A4%A4%E0%A4%BE%E0%A4%B2",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A4%BE%E0%A4%AF%E0%A4%B5%E0%A5%8D%E0%A4%AF%E0%A5%82%E0%A4%B9",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A4%BE%E0%A4%B0%E0%A5%8D%E0%A4%AF%E0%A5%8D%E0%A4%AF%E0%A4%95%E0%A4%BE%E0%A4%B0%E0%A4%A3%E0%A4%AD%E0%A4%BE%E0%A4%B5",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A4%BE%E0%A4%B2%E0%A4%AF%E0%A5%8B%E0%A4%97",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A4%BE%E0%A4%B8%E0%A4%BE%E0%A4%B0",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A5%81%E0%A4%A3",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A5%81%E0%A4%AE%E0%A5%8D%E0%A4%AD%E0%A4%95",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A5%81%E0%A4%B7%E0%A4%B2",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A5%83%E0%A4%AA%E0%A4%BE%E0%A4%B2%E0%A5%81",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A5%87%E0%A4%A4%E0%A5%81%E0%A4%95%E0%A5%81%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%B2%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A5%8B%E0%A4%B8%E0%A4%B2",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A5%8D%E0%A4%B0%E0%A4%BE%E0%A4%A8%E0%A5%8D%E0%A4%A4%E0%A4%BF%E0%A4%95%E0%A5%8D%E0%A4%B7%E0%A5%87%E0%A4%A4%E0%A5%8D%E0%A4%B0",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A5%8D%E0%A4%B7%E0%A4%A3",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%95%E0%A5%8D%E0%A4%B7%E0%A5%80%E0%A4%B0%E0%A4%B2%E0%A4%A4%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%96",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%96%E0%A4%9A",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%97",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%97%E0%A4%A6%E0%A4%BE%E0%A4%AD%E0%A5%83%E0%A4%A4%E0%A5%8D",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%97%E0%A4%B0%E0%A5%8D%E0%A4%AD%E0%A4%B2%E0%A4%95%E0%A5%8D%E0%A4%B7%E0%A4%A3",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%97%E0%A4%BF%E0%A4%B0%E0%A4%BF%E0%A4%B0%E0%A4%BE%E0%A4%9C",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%97%E0%A5%81%E0%A4%B9%E0%A4%BE%E0%A4%9A%E0%A4%B0",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%97%E0%A5%83%E0%A4%B9%E0%A4%B5%E0%A4%BE%E0%A4%9F%E0%A4%BF%E0%A4%95%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%97%E0%A5%8B%E0%A4%A4%E0%A5%8D%E0%A4%B0%E0%A4%9C",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%97%E0%A5%8C%E0%A4%B0",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%98",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%9A",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%9A%E0%A4%95%E0%A5%8D%E0%A4%B0%E0%A4%B5%E0%A5%8D%E0%A4%AF%E0%A5%82%E0%A4%B9",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%9A%E0%A4%A4%E0%A5%81%E0%A4%B0%E0%A5%8D%E0%A4%AD%E0%A4%BE%E0%A4%B5",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%9A%E0%A4%BE%E0%A4%A4%E0%A5%81%E0%A4%B0",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%9A%E0%A4%BF%E0%A4%A4%E0%A5%8D%E0%A4%B0%E0%A4%95%E0%A4%BE%E0%A4%B0",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%9B",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%9C",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%9C%E0%A4%AE%E0%A4%A8",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%9C%E0%A4%B2%E0%A4%BE%E0%A4%B7",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%9C%E0%A4%BE%E0%A4%B2%E0%A4%BF%E0%A4%A8%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%9C%E0%A5%8D%E0%A4%9E%E0%A4%BE%E0%A4%A8%E0%A4%AF%E0%A4%9C%E0%A5%8D%E0%A4%9E",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%9D-%E0%A4%A3",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A4",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A4%E0%A4%AA%E0%A5%8B%E0%A4%AE%E0%A5%82%E0%A4%B0%E0%A5%8D%E0%A4%A4%E0%A5%8D%E0%A4%A4%E0%A4%BF",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A4%E0%A4%BE%E0%A4%A8",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A4%E0%A4%BF%E0%A4%B0%E0%A4%B8",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A4%E0%A5%81%E0%A4%B7%E0%A5%8D%E0%A4%9F",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A4%E0%A5%8D%E0%A4%B0%E0%A4%BF%E0%A4%AA%E0%A4%B0%E0%A5%8D%E0%A4%A3",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A5",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A6",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A6%E0%A4%A3%E0%A5%8D%E0%A4%A1%E0%A4%B9%E0%A4%B8%E0%A5%8D%E0%A4%A4",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A6%E0%A4%B0%E0%A4%BF%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A6%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A6%E0%A4%BE%E0%A4%A8%E0%A5%81",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A6%E0%A4%BF%E0%A4%A6%E0%A5%8D%E0%A4%AF%E0%A5%81",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A6%E0%A5%80%E0%A4%B0%E0%A5%8D%E0%A4%98%E0%A4%B6%E0%A4%B0",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A6%E0%A5%81%E0%A4%B5%E0%A5%8B%E0%A4%AF%E0%A5%81",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A6%E0%A5%87%E0%A4%B5%E0%A4%A4%E0%A4%BE%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%A4%E0%A4%BF%E0%A4%B7%E0%A5%8D%E0%A4%A0%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A6%E0%A5%88%E0%A4%B5%E0%A4%A6%E0%A4%BE%E0%A4%B0%E0%A4%B5",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A6%E0%A5%8D%E0%A4%B5%E0%A4%A8%E0%A5%8D%E0%A4%A6",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A7",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A7%E0%A4%B0%E0%A5%8D%E0%A4%AE%E0%A4%B8%E0%A5%87%E0%A4%A4%E0%A5%81",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A7%E0%A5%82%E0%A4%95",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A8",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A8%E0%A4%A6%E0%A5%80%E0%A4%A6%E0%A5%8B%E0%A4%B9",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A8%E0%A4%B5%E0%A4%B2%E0%A4%95%E0%A5%8D%E0%A4%B7%E0%A4%A3",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A8%E0%A4%BE%E0%A4%A8%E0%A5%8D%E0%A4%A6%E0%A5%80",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A8%E0%A4%BF%E0%A4%A6%E0%A5%87%E0%A4%B6",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A8%E0%A4%BF%E0%A4%B0%E0%A5%8D%E0%A4%AE%E0%A4%BE%E0%A4%A3",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A8%E0%A5%80%E0%A4%9A%E0%A4%97%E0%A5%83%E0%A4%B9",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%A8%E0%A5%88%E0%A4%95",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AA",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AA%E0%A4%A4%E0%A4%BE%E0%A4%95%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AA%E0%A4%B0%E0%A4%BF%E0%A4%9A%E0%A5%8D%E0%A4%9B%E0%A5%87%E0%A4%A6",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AA%E0%A4%B6%E0%A5%8D%E0%A4%B5%E0%A4%AF%E0%A4%A8",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AA%E0%A4%BE%E0%A4%B0%E0%A5%81",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AA%E0%A5%81%E0%A4%9F",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AA%E0%A5%81%E0%A4%B7",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AA%E0%A5%83%E0%A4%B7%E0%A4%AD%E0%A4%BE%E0%A4%B7%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%A4%E0%A4%BF%E0%A4%AC%E0%A5%81%E0%A4%A6%E0%A5%8D%E0%A4%A7",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%AF%E0%A4%B8%E0%A5%8D",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%BE%E0%A4%A3%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%A6",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AB",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AC",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AC%E0%A5%81%E0%A4%A7",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AD",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AD%E0%A4%BE%E0%A4%97",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AD%E0%A5%82%E0%A4%A4",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AE",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AE%E0%A4%B9%E0%A4%BE%E0%A4%A7%E0%A4%BE%E0%A4%A4%E0%A5%81",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%AF",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B0",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B2",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B5",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B5%E0%A4%B9%E0%A4%A4%E0%A4%BF",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B5%E0%A4%BF%E0%A4%9C%E0%A5%8D%E0%A4%9E%E0%A4%BE%E0%A4%A8",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B5%E0%A4%BF%E0%A4%B7%E0%A4%AE%E0%A4%9C%E0%A5%8D%E0%A4%B5%E0%A4%B0",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B5%E0%A5%87%E0%A4%A6%E0%A4%BE%E0%A4%99%E0%A5%8D%E0%A4%97",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B5%E0%A5%8D%E0%A4%B0%E0%A4%A4",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B6",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B6%E0%A4%BE%E0%A4%B2%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B6%E0%A5%83%E0%A4%99%E0%A5%8D%E0%A4%97",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B7",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B8",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B8%E0%A4%A6%E0%A4%BE%E0%A4%AB%E0%A4%B2",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B8%E0%A4%AE%E0%A5%8D%E0%A4%AA%E0%A5%8D%E0%A4%B0%E0%A4%A4%E0%A4%BF",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B8%E0%A4%BE%E0%A4%A4%E0%A5%8D%E0%A4%A4%E0%A5%8D%E0%A4%B5%E0%A4%A4",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B8%E0%A5%81%E0%A4%9A%E0%A4%BF%E0%A4%A4%E0%A5%8D%E0%A4%B0%E0%A4%BE",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B8%E0%A5%8D%E0%A4%A4%E0%A4%BE%E0%A4%B5%E0%A4%95",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B8%E0%A5%8D%E0%A4%AB%E0%A4%BE%E0%A4%A4%E0%A4%BF",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B9",
        "https://sa.wikisource.org/wiki/%E0%A4%B5%E0%A4%BE%E0%A4%9A%E0%A4%B8%E0%A5%8D%E0%A4%AA%E0%A4%A4%E0%A5%8D%E0%A4%AF%E0%A4%AE%E0%A5%8D/%E0%A4%B9%E0%A4%B0%E0%A4%BF%E0%A4%A6%E0%A5%8D%E0%A4%B0%E0%A4%BE"
        ],
        fileNamePrefix: "vAchaspatya-",
        numFiles: -1,
        numDigits: 1
    }   
    ]
    ;

function getAllUrls(startURL, numItems, minNumDigits){
    var urls = [];
    var prefix = "%E0%A5%A";
    for(let i=1;i<numItems+1;i++){
        let suffix = "";
        let str = i.toString();
        for(let x=str.length;x<minNumDigits;x++)
            str = "0"+str;
        // Yes its str.length and NOT minNumDigits
        for(let j=0;j<str.length;j++){
            suffix += prefix + (parseInt(str.charAt(j))+6).toString(16);
        }
        //console.log(i+": "+suffix);
        urls.push(startURL+suffix);
    }
    return urls;
}

function downloadContent(uri){
    return rp(uri).then(function (htmlString) {
            // Process html... 
            return htmlString;
        })
        .catch(function (err) {
            // Crawling failed... 
            console.error("downloadContent failed: " + err);
            console.error("failed uri:"+ uri);
        });
}

async function downloadAllContent(){
    for(urlInfoItem of urlInfo){
        let minNumDigits = urlInfoItem.numDigits;
        let numFiles = urlInfoItem.numFiles;
        let urlList = [];
        if( urlInfoItem.hasOwnProperty('urlList') ){
            urlList = urlInfoItem.urlList;
            numFiles = urlList.length;
        }
        else if( urlInfoItem.hasOwnProperty('urlPrefix') )
            urlList = getAllUrls(urlInfoItem.urlPrefix, numFiles, minNumDigits);
        let fileNamePrefix = urlInfoItem.fileNamePrefix;
        let totalDigits = urlList.length.toString().length;
        for(let i=1;i<urlList.length+1;i++){
            let str = i.toString();
            for(let x=str.length;x<totalDigits;x++)
                str = "0"+str;
            let filePath = fileNamePrefix+str+".txt";  
            let htmlString = await downloadContent(urlList[i-1]);
            let text = htmlToText.fromString(htmlString);
            fs.writeFileSync(filePath, "\uFEFF"+text, {encoding: 'ucs2'});
            console.log(urlList[i-1]);
        }
    }
}

downloadAllContent().then(() => {
    console.log("Done");
}).catch((err) => {
    console.error(err);
});
//fs.writeFileSync("test.txt", "\uFEFFTest", "utf16le");