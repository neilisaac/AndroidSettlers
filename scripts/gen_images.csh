#!/bin/csh

set lang = ""
if ($#argv == 1) then
	set lang = "-$1"
endif

foreach file (`ls res/drawable${lang}-hdpi`)
	set identify = `identify res/drawable${lang}-hdpi/$file`
	set width = `echo $identify | cut -d ' ' -f 3 | cut -d 'x' -f 1`
	set height = `echo $identify | cut -d ' ' -f 3 | cut -d 'x' -f 2`

	set mdpiwidth = `echo $width \* 2 / 3 | bc`
	set mdpiheight = `echo $height \* 2 / 3 | bc`
	
	set ldpiwidth = `echo $width / 2 | bc`
	set ldpiheight = `echo $height / 2 | bc`

	echo $file - hdpi: ${width}x${height}  mdpi: ${mdpiwidth}x${mdpiheight}  ldpi: ${ldpiwidth}x${ldpiheight}

	convert -resize ${mdpiwidth}x${mdpiheight} res/drawable${lang}-hdpi/$file res/drawable${lang}-mdpi/$file
	convert -resize ${ldpiwidth}x${ldpiheight} res/drawable${lang}-hdpi/$file res/drawable${lang}-ldpi/$file
end

