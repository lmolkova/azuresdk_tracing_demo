#makedir
WORKDIR /python

# install
COPY requirements.txt .
RUN pip install -r requirements.txt

# copy source code
COPY /python .

EXPOSE 8000
ENV PORT=8000 
# run
CMD ["python", "manage.py", "runserver"]
