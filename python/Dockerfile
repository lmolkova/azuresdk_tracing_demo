FROM python:3

#makedir
WORKDIR /python

# install
COPY requirements.txt .
RUN pip install -r requirements.txt

# copy source code
COPY . .

EXPOSE 8000

# run
CMD ["python", "manage.py", "runserver"]
