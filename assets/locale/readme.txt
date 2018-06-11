Area Protection  -  A Rising World Java plug-in to define and protect areas.

Localisation / Translation

The language of the user interface can be changed. The plug-in uses a
separate string file for each language in the   locale   sub-folder. The
plug-in comes with a few files ready to contain the strings for some
languages (but not translated yet). If the string file for your language
is not provided, add it as follows:

1) Copy the   messages_en.properties   into a file replacing 'en' with
   the code of your language (a list of 'official' language codes can be
   found at:  https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes).
   Examples:
   
   messages_pt.properties     (for Portuguese)
     OR   
   messages_es.properties     (for Spanish)

   Optional: If the language has national or regional variants and you
   want to distinguish between them, add also the code for the country.
   Example:

   messages_pt_PT.properties  (for Portugal Portuguese)
     OR
   messages_pt_BR.properties  (for Brasilian Portuguese)

   Codes for countries can be found at
   https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2

2) Open the file with a text editor and translate each string (the part
   at the right of the '='), while keeping codes like "\n" or "%1$d".

3) Edit the   settings.properties   file to have the "locale=..." key
   to point to the language code. Example:
   "locale=pt"
     OR
   "locale=pt_BR"

   Be sure to spell the locale EXACTLY as used in the name of the string
   file, CASE Is AlSo ImPoRtAnT !!

4) Restart the server or use the "reloadplugins" to put the change into
   effect.
