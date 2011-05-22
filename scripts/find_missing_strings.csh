#!/bin/csh

set LANGUAGES = "de es nl zh ko"
set IGNORE = "app_name copyright nostring website website_url acknowledgements translators"

foreach lang ($LANGUAGES)
	echo TRANSLATION $lang
	
	set file = "res/values-$lang/strings.xml"
	set strings = `grep 'name=' res/values/strings.xml | sed 's/^.*name="//' | sed 's/".*$//'`
	
	foreach string ($strings)
		if (`echo $IGNORE | grep $string` == "") then
			set result = `grep '"'$string'"' $file`
			if ("$result" == "") then
				echo "MISSING $string"
			endif
		endif
	end
	
	echo
end
 
