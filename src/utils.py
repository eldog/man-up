import datetime
import os

def get_path(origin_path, relative_path):
    return os.path.join(os.path.dirname(origin_path), relative_path)

def path_getter(origin_path):
    def getter(relative_path):
        return get_path(origin_path, relative_path)
    return getter

def parse_date(date_string):
    return datetime.datetime.strptime(date_string, '%Y-%m-%d').date()
    
def generate_content_dict(model, page_num, content_per_page, message):
    model_query = model.all().order('-date');
    count = model_query.count();
    
    #will need fixing if they ever move to Python3
    max_num = count / content_per_page
    
    if page_num > max_num:
        page_num = max_num
        message = 'That page doesn\'t exist, why not look at the last page again.'
    elif page_num < 0:
        page_num = 0
        message = 'That page doesn\'t exist, why not look at the first page again.'
    
    if page_num == max_num:
        hasNext = False
    else:
        hasNext = True
    if page_num == 0:
        hasPrev = False
    else:
        hasPrev = True
    
    range_min = page_num * content_per_page
    range_max = (page_num + 1) * content_per_page
    
    content_list = model_query.fetch(range_max,range_min)
    pagination_dict = { 'num' : page_num, 
                        'prev' : (page_num-1), 
                        'next' : (page_num+1),
                        'hasNext' : hasNext,
                        'hasPrev' : hasPrev }
    
    if message != None:
        return { 'content_list' : content_list, 'pagedata' : pagination_dict, 
                 'message' : message }
    else:
        return { 'content_list' : content_list, 'pagedata' : pagination_dict }
