'''
This file is part of Pac Defence.

Pac Defence is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Pac Defence is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Pac Defence.  If not, see <http://www.gnu.org/licenses/>.

(C) Liam Byrne, 2008 - 10.
'''

'''
This script is used to generate the web pages for the site from the .part.html files. It must be run
from the directory it is in or it won't work properly.

The .part.html files are Django templates, currently with no context, just there so that I can
extend base.html overriding specific blocks where necessary.

Django is required to run this script, under Ubuntu it can easily be installed via the package
python-django.
'''

from django.conf import settings
from django.template import Context, loader

import os

# The extension for the part files
EXTENSION = '.part.html'

# Set up some default settings
settings.configure(TEMPLATE_DIRS=('.'))

# Find each part file in the directory, render the contents to the non-part file
for filename in os.listdir('.'):
    if filename.endswith(EXTENSION):
        template = loader.get_template(filename)
        
        # Remove the extension and just put on .part.html
        new_file_name = filename[:-len(EXTENSION)] + '.html'
        
        new_file = open(new_file_name, 'w')
        new_file.write(template.render(Context({})))
        new_file.close()
        
        print 'Wrote ' + new_file_name
        